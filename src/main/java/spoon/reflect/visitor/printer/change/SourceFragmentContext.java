package spoon.reflect.visitor.printer.change;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

/**
 * Knows how to handle actually printed {@link CtElement} or it's part
 */
interface SourceFragmentContext {
	/**
	 * Called when {@link DefaultJavaPrettyPrinter} starts an operation
	 * @param event the {@link DefaultJavaPrettyPrinter} event
	 */
	void onPrintEvent(PrinterEvent event);

	/**
	 * Called when this is child context and parent context is just going to finish it's printing
	 */
	void onFinished();

	/**
	 * @return true if this context can handle `role`
	 */
	boolean matchesPrinterEvent(PrinterEvent event);
}
