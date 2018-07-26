package spoon.reflect.visitor.printer.change;

import spoon.reflect.code.CtComment;
import spoon.reflect.visitor.PrinterHelper;
import spoon.reflect.visitor.TokenWriter;

class TokenWriterProxy implements TokenWriter {
	/**
	 * Listens for each call of {@link TokenWriter}
	 */
	interface Listener {
		/**
		 * Called once for each call of {@link TokenWriter} method
		 * @param tokenType identifies the called method
		 * @param token the sent token. May be null
		 * @param comment the sent comment. Is null for tokenType != {@link TokenType#COMMENT}
		 * @param printAction a {@link Runnable}, which can be used to run wrapped {@link TokenWriter} method
		 */
		void onTokenWriterWrite(TokenType tokenType, String token, CtComment comment, Runnable printAction);
	}

	private final Listener listener;
	private final TokenWriter delegate;

	TokenWriterProxy(Listener listener, TokenWriter delegate) {
		super();
		this.listener = listener;
		this.delegate = delegate;
	}

	public TokenWriter writeSeparator(String token) {
		this.listener.onTokenWriterWrite(TokenType.SEPARATOR, token, null, () -> delegate.writeSeparator(token));
		return this;
	}

	public TokenWriter writeOperator(String token) {
		this.listener.onTokenWriterWrite(TokenType.OPERATOR, token, null, () -> delegate.writeOperator(token));
		return this;
	}

	public TokenWriter writeLiteral(String token) {
		this.listener.onTokenWriterWrite(TokenType.LITERAL, token, null, () -> delegate.writeLiteral(token));
		return this;
	}

	public TokenWriter writeKeyword(String token) {
		this.listener.onTokenWriterWrite(TokenType.KEYWORD, token, null, () -> delegate.writeKeyword(token));
		return this;
	}

	public TokenWriter writeIdentifier(String token) {
		this.listener.onTokenWriterWrite(TokenType.IDENTIFIER, token, null, () -> delegate.writeIdentifier(token));
		return this;
	}

	public TokenWriter writeCodeSnippet(String token) {
		this.listener.onTokenWriterWrite(TokenType.CODE_SNIPPET, token, null, () -> delegate.writeCodeSnippet(token));
		return this;
	}

	public TokenWriter writeComment(CtComment comment) {
		this.listener.onTokenWriterWrite(TokenType.COMMENT, null, comment, () -> delegate.writeComment(comment));
		return this;
	}

	public TokenWriter writeln() {
		this.listener.onTokenWriterWrite(TokenType.NEW_LINE, "\n", null, () -> delegate.writeln());
		return this;
	}

	public TokenWriter incTab() {
		this.listener.onTokenWriterWrite(TokenType.INC_TAB, null, null, () -> delegate.incTab());
		return this;
	}

	public TokenWriter decTab() {
		this.listener.onTokenWriterWrite(TokenType.DEC_TAB, null, null, () -> delegate.decTab());
		return this;
	}

	public PrinterHelper getPrinterHelper() {
		return delegate.getPrinterHelper();
	}

	public void reset() {
		delegate.reset();
	}

	public TokenWriter writeSpace() {
		this.listener.onTokenWriterWrite(TokenType.SPACE, " ", null, () -> delegate.writeSpace());
		return this;
	}
}
