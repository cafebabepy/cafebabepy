package org.cafebabepy.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yotchang4s on 2017/07/19.
 */
public class CafeBabePyLexer extends PythonLexer {

    private LinkedList<Token> tokens = new LinkedList<>();

    private LinkedList<Integer> indents = new LinkedList<>();

    private List<Integer> unmodifiableIndents = Collections.unmodifiableList(this.indents);

    private StringBuilder newLineBuilder = new StringBuilder();

    private StringBuilder spacesBuilder = new StringBuilder();

    private int opened = 0;

    private Token lastToken = null;

    private boolean eof = false;

    public CafeBabePyLexer(CharStream input) {
        super(input);
    }

    public List<Integer> getIndents() {
        return this.unmodifiableIndents;
    }

    public boolean isOpened() {
        return this.opened > 0;
    }

    @Override
    public void emit(Token t) {
        super.setToken(t);
        this.tokens.offer(t);
    }

    @Override
    public Token nextToken() {
        boolean atStartOfInput = getCharPositionInLine() == 0 && getLine() == 1;
        this.newLineBuilder.setLength(0);
        this.spacesBuilder.setLength(0);

        int la = this._input.LA(1);
        if (la == '(' || la == '[' || la == '{') {
            this.opened++;

        } else if (la == ')' || la == ']' || la == '}') {
            this.opened--;
        }

        int newLineCharIndex = -1;
        do {
            if (la == '\r') {
                this.newLineBuilder.append((char) la);
                newLineCharIndex = getCharIndex();

                this._input.consume();
                la = this._input.LA(1);

                if (la == '\n') {
                    this.newLineBuilder.append((char) la);
                    newLineCharIndex = getCharIndex();

                    this._input.consume();
                    la = this._input.LA(1);

                }

            } else if (la == '\n' || la == '\f') {
                this.newLineBuilder.append((char) la);
                newLineCharIndex = getCharIndex();

                this._input.consume();
                la = this._input.LA(1);

            } else {
                break;
            }

        } while (true);

        int spaceCharIndex = -1;
        do {
            if (la == ' ' || la == '\t') {
                this.spacesBuilder.append((char) la);
                spaceCharIndex = getCharIndex();

                this._input.consume();
                la = this._input.LA(1);

            } else {
                break;
            }

        } while (true);

        if ((this.newLineBuilder.length() > 0)
                || (atStartOfInput && this.spacesBuilder.length() > 0)) {

            String newLine = this.newLineBuilder.toString();
            String spaces = this.spacesBuilder.toString();
            int indent = getIndentCount(spaces);
            int previous = this.indents.isEmpty() ? 0 : this.indents.peekFirst();

            if (!isOpened() && this.newLineBuilder.length() > 0) {
                emit(getCommonToken(NEWLINE, newLine, newLineCharIndex));
                this.newLineBuilder.setLength(0);
            }

            if (indent == previous) {
                // skip

            } else if (indent > previous) {
                this.indents.addFirst(indent);
                emit(getCommonToken(INDENT, spaces, spaceCharIndex));

            } else {
                while (!this.indents.isEmpty() && this.indents.peekFirst() > indent) {
                    CommonToken dedent = new CommonToken(DEDENT, "<DEDENT>");
                    dedent.setLine(getLine());
                    emit(dedent);
                    this.indents.removeFirst();
                }
            }

        } else {
            if (!isOpened() && this.newLineBuilder.length() > 0) {
                emit(getCommonToken(NEWLINE, newLineBuilder.toString(), newLineCharIndex));
                this.newLineBuilder.setLength(0);
            }
        }

        if (la == EOF && !this.indents.isEmpty()) {
            for (int i = this.tokens.size() - 1; i >= 0; i--) {
                if (this.tokens.get(i).getType() == EOF) {
                    this.tokens.remove(i);
                }
            }

            CommonToken newLine = new CommonToken(NEWLINE, System.lineSeparator());
            newLine.setLine(this.lastToken.getLine());
            emit(newLine);

            while (!this.indents.isEmpty()) {
                CommonToken dedent = new CommonToken(DEDENT, "<DEDENT>");
                dedent.setLine(this.lastToken.getLine());
                emit(dedent);

                this.indents.removeFirst();
            }

            CommonToken eof = new CommonToken(EOF, "<EOF>");
            eof.setLine(this.lastToken.getLine());
            emit(eof);

            this.eof = true;

        } else {
            if (la == EOF && !this.eof) {
                CommonToken newLine = new CommonToken(NEWLINE, System.lineSeparator());
                newLine.setLine(this.lastToken.getLine());
                emit(newLine);

                this.eof = true;
            }
        }

        Token next = super.nextToken();

        if (next.getChannel() == Token.DEFAULT_CHANNEL) {
            this.lastToken = next;
        }

        Token result = this.tokens.isEmpty() ? next : this.tokens.poll();


        return result;
    }

    private CommonToken getCommonToken(int type, String text, int charIndex) {
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
