package org.amshove.natls.callhierarchy;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CallHierarchyShould extends LanguageServerTest
{
	private static LspTestContext testContext;

	@Test
	void createHierarchyItemsForIncomingCalls()
	{
		var documentIdentifier = textDocumentIdentifier("LIBTWO", "CALLED");
		var item = prepareCallHierarchy(documentIdentifier);
		var incoming = getIncomingCalls(item);

		assertThat(incoming)
			.anyMatch(ic -> isFromFileAndLine(ic, textDocumentIdentifier("LIBTWO", "SUBC1"), 7))
			.anyMatch(ic -> isFromFileAndLine(ic, textDocumentIdentifier("LIBTWO", "SUBC2"), 7))
			.anyMatch(ic -> isFromFileAndLine(ic, textDocumentIdentifier("LIBTWO", "SUBC2"), 11));
	}

	@Test
	void createHierarchyItemsForOutgoingCalls()
	{
		var documentIdentifier = textDocumentIdentifier("LIBTWO", "SUBC2");
		var item = prepareCallHierarchy(documentIdentifier);
		var outgoing = getOutgoingCalls(item);

		assertThat(outgoing)
			.anyMatch(oc -> isToFileAndFromLine(oc, documentIdentifier, 7))
			.anyMatch(oc -> isToFileAndFromLine(oc, documentIdentifier, 11));
	}

	@BeforeAll
	static void setupProject(@LspProjectName("callhierarchy") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	private CallHierarchyItem prepareCallHierarchy(TextDocumentIdentifier identifier)
	{
		var params = new CallHierarchyPrepareParams(identifier, new Position(0, 0));
		try
		{
			var result = testContext.documentService().prepareCallHierarchy(params).get(5, TimeUnit.SECONDS);
			assertThat(result).hasSize(1);
			return result.get(0);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private List<CallHierarchyIncomingCall> getIncomingCalls(CallHierarchyItem item)
	{
		try
		{
			return testContext.documentService().callHierarchyIncomingCalls(new CallHierarchyIncomingCallsParams(item)).get(5, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private List<CallHierarchyOutgoingCall> getOutgoingCalls(CallHierarchyItem item)
	{
		try
		{
			return testContext.documentService().callHierarchyOutgoingCalls(new CallHierarchyOutgoingCallsParams(item)).get(5, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private boolean isFromFileAndLine(CallHierarchyIncomingCall item, TextDocumentIdentifier identifier, int line)
	{
		return item.getFrom().getUri().equals(identifier.getUri())
			&& item.getFromRanges().get(0).getStart().getLine() == line;
	}

	private boolean isToFileAndFromLine(CallHierarchyOutgoingCall item, TextDocumentIdentifier identifier, int line)
	{
		return item.getTo().getUri().equals(identifier.getUri())
			&& item.getFromRanges().get(0).getStart().getLine() == line;
	}
}
