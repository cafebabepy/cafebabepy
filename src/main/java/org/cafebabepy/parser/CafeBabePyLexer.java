package org.cafebabepy.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.cafebabepy.parser.antlr.PythonLexer;

import java.util.LinkedList;

/**
 * Created by yotchang4s on 2017/07/19.
 */
public class CafeBabePyLexer extends PythonLexer {

    private LinkedList<Token> tokens = new LinkedList<>();

    private LinkedList<Integer> indents = new LinkedList<>();

    private StringBuilder newLineBuilder = new StringBuilder();

    private int newLineCharIndex = -1;

    private StringBuilder spacesBuilder = new StringBuilder();

    private int spaceCharIndex = -1;

    private int opened = 0;

    private int lineJoining = 0;

    private Token lastToken = null;

    private boolean eof = false;

    CafeBabePyLexer(CharStream input) {
        super(input);
    }

    boolean isOpened() {
        return this.opened > 0;
    }

    boolean isLineJoining() {
        return this.lineJoining > 0;
    }

    @Override
    public void emit(Token token) {
        Token result = token;
        int resultType = token.getType();

        if (resultType == OPEN_PAREN || resultType == OPEN_BRACK || resultType == OPEN_BRACE) {
            this.opened++;

        } else if (resultType == CLOSE_PAREN || resultType == CLOSE_BRACK || resultType == CLOSE_BRACE) {
            this.opened--;

        } else if (resultType == PHYSICAL_NEWLINE) {
            if (this.opened > 0) {
                // nextToken result is null
                return;

            } else {
                result = nextToken();
            }

        } else if (resultType == LINE_JOINING) {
            this.lineJoining++;
            return;
        }

        emitOnly(result);
    }

    private void emitOnly(Token token) {
        super.setToken(token);
        this.tokens.offer(token);
    }

    @Override
    public Token nextToken() {
        boolean atStartOfInput = getCharPositionInLine() == 0 && getLine() == 1;

        readNewLine();

        if (this.opened == 0
                && (this.newLineBuilder.length() > 0 || atStartOfInput)) {

            int la = readSpaces();

            if (atStartOfInput && this.spacesBuilder.length() == 0) {
                // skip

            } else {
                if (la == '\r' || la == '\n' || la == '\f') {
                    return nextToken();

                } else if (la == '#') {
                    return super.nextToken();
                }

                String spaces = this.spacesBuilder.toString();
                int indent = getIndentCount(spaces);
                int previous = this.indents.isEmpty() ? 0 : this.indents.peekFirst();

                if (indent == previous) {
                    newLine(false);
                    // skip

                } else if (indent > previous) {
                    if (la != EOF) {
                        newLine(false);
                        this.indents.addFirst(indent);
                        emitOnly(createCommonToken(INDENT, spaces, this.spaceCharIndex));
                    }

                } else {
                    newLine(false);
                    while (!this.indents.isEmpty() && this.indents.peekFirst() > indent) {
                        CommonToken dedent = new CommonToken(DEDENT, "<DEDENT>");
                        dedent.setLine(getLine());
                        emitOnly(dedent);
                        this.indents.removeFirst();
                    }
                }
            }
        }

        Token next;
        while ((next = super.nextToken()) == null) ;
        int la = this._input.LA(1);

        if (la == EOF) {
            if (!this.eof) {
                for (int i = this.tokens.size() - 1; i >= 0; i--) {
                    if (this.tokens.get(i).getType() == EOF) {
                        this.tokens.remove(i);
                    }
                }
                if (!this.indents.isEmpty()) {
                    newLine(true);

                    while (!this.indents.isEmpty()) {
                        CommonToken dedent = new CommonToken(DEDENT, "<DEDENT>");
                        int dedentLine = (this.lastToken != null) ? this.lastToken.getLine() : getLine();
                        dedent.setLine(dedentLine);
                        emitOnly(dedent);

                        this.indents.removeFirst();
                    }

                    newEof();

                } else {
                    newLine(true);
                    newEof();
                }

                this.eof = true;
            }
        }

        if (next.getChannel() == Token.DEFAULT_CHANNEL) {
            this.lastToken = next;
        }

        Token token = this.tokens.isEmpty() ? next : this.tokens.poll();

        return token;
    }

    private int readSpaces() {
        this.spacesBuilder.setLength(0);
        this.spaceCharIndex = -1;

        int la = this._input.LA(1);

        do {
            if (la == ' ' || la == '\t') {
                this.spacesBuilder.appendCodePoint(la);
                this.spaceCharIndex = getCharIndex();

                this._input.consume();
                la = this._input.LA(1);

            } else {
                return la;
            }

        } while (true);
    }

    private int readNewLine() {
        this.newLineBuilder.setLength(0);
        this.newLineCharIndex = -1;

        int la = _input.LA(1);
        do {
            if (la == '\r') {
                this.newLineBuilder.appendCodePoint(la);
                this.newLineCharIndex = getCharIndex();

                this._input.consume();
                la = this._input.LA(1);

                if (la == '\n') {
                    this.newLineBuilder.appendCodePoint(la);
                    this.newLineCharIndex = getCharIndex();

                    this._input.consume();
                    la = this._input.LA(1);
                }

            } else if (la == '\n' || la == '\f') {
                this.newLineBuilder.appendCodePoint(la);
                this.newLineCharIndex = getCharIndex();

                this._input.consume();
                la = this._input.LA(1);

            } else {
                if (this.newLineBuilder.length() > 0 && this.lineJoining > 0) {
                    this.lineJoining = 0;
                }
                return la;
            }

        } while (true);
    }

    private void newLine(boolean createNewLIne) {
        if (this.opened == 0 && this.newLineBuilder.length() > 0) {
            emitOnly(createCommonToken(NEWLINE, this.newLineBuilder.toString(), this.newLineCharIndex));
            this.newLineBuilder.setLength(0);

        } else if (createNewLIne) {
            CommonToken newLine = new CommonToken(NEWLINE, "\n");
            if (this.lastToken != null) {
                newLine.setLine(this.lastToken.getLine());

            } else {
                newLine.setLine(getLine());
            }

            emitOnly(newLine);
        }
    }

    private void newEof() {
        CommonToken eof = new CommonToken(EOF, "<EOF>");
        int eofLine = (this.lastToken != null) ? this.lastToken.getLine() : getLine();
        eof.setLine(eofLine);
        emitOnly(eof);
    }

    private CommonToken createCommonToken(int type, String text, int charIndex) {
        int stop = charIndex;
        int start = text.isEmpty() ? stop : stop - text.length() + 1;
        return new CommonToken(this._tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, start, stop);
    }

    private static int getIndentCount(String spaces) {
        int count = 0;

        for (char ch : spaces.toCharArray()) {
            switch (ch) {
                case '\t':
                    count += 8 - (count % 8);
                    break;

                default:
                    count++;
            }
        }

        return count;
    }
}
