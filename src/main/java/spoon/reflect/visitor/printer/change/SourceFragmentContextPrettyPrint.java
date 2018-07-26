package spoon.reflect.visitor.printer.change;

/**
 * Prints the element using standard pretty printing
 */
class SourceFragmentContextPrettyPrint implements SourceFragmentContext {
	/**
	 * This context is used to force normal pretty printing of element
	 */
	static final SourceFragmentContextPrettyPrint INSTANCE = new SourceFragmentContextPrettyPrint();

	private SourceFragmentContextPrettyPrint() {
	}

	@Override
	public void onPrintEvent(PrinterEvent event) {
		event.print(null);
	}

	@Override
	public void onFinished() {
	}

	@Override
	public boolean matchesPrinterEvent(PrinterEvent event) {
		return true;
	}
}
