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

    private int opened = 0;

    private int lineJoining = 0;

    private boolean eof = false;

    CafeBabePyLexer(CharStream input) {
        super(input);
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

    boolean isOpened() {
        return this.opened > 0;
    }

    boolean isLineJoining() {
        return this.lineJoining > 0;
    }

    @Override
    public void emit(Token token) {
        int type = token.getType();

        if (type == OPEN_PAREN || type == OPEN_BRACK || type == OPEN_BRACE) {
            this.opened++;

        } else if (type == CLOSE_PAREN || type == CLOSE_BRACK || type == CLOSE_BRACE) {
            this.opened--;

        } else if (type == LINE_JOINING) {
            this.lineJoining++;
            return;

        } else if (type == PHYSICAL_NEWLINE) {
            if (this.opened == 0) {
                emitNewLine();
            }

            return;

        } else if (type == EOF) {
            if (this.eof) {
                return;
            }
            this.tokens.removeIf(t -> t.getType() == EOF);

            if (this._input.LA(1) == EOF) {
                this.eof = true;
            }
            emitNewLine();

            while (!this.indents.isEmpty()) {
                CommonToken dedent = new CommonToken(DEDENT, "<DEDENT>");
                emitOnly(dedent);
                this.indents.removeFirst();
            }

            Token eof = new CommonToken(EOF, "<EOF>");
            emitOnly(eof);
        }

        emitOnly(token);
    }

    private void emitOnly(Token token) {
        super.setToken(token);
        this.tokens.offer(token);
    }

    @Override
    public Token nextToken() {
        Token next = super.nextToken();
        while (next == null) {
            next = super.nextToken();
        }

        return this.tokens.isEmpty() ? null : this.tokens.poll();
    }

    private void emitNewLine() {
        StringBuilder spacesBuilder = new StringBuilder();

        int la = this._input.LA(1);

        while (la == ' ' || la == '\t') {
            spacesBuilder.appendCodePoint(la);

            this._input.consume();
            la = this._input.LA(1);
        }

        if (la == '\r' || la == '\n' || la == '\f' || la == '#') {
            return;
        }

        String spaces = spacesBuilder.toString();
        int indent = getIndentCount(spaces);
        int previous = this.indents.isEmpty() ? 0 : this.indents.peekFirst();

        CommonToken newLine = new CommonToken(NEWLINE, "<NEWLINE>");
        emitOnly(newLine);

        if (indent > previous) {
            this.indents.addFirst(indent);
            CommonToken indentToken = new CommonToken(INDENT, "<INDENT>");
            emitOnly(indentToken);

        } else {
            while (!this.indents.isEmpty() && this.indents.peekFirst() > indent) {
                CommonToken dedentToken = new CommonToken(DEDENT, "<DEDENT>");
                emitOnly(dedentToken);
                this.indents.removeFirst();
            }
        }
    }
}
