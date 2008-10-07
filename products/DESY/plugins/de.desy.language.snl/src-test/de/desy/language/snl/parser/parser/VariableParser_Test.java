package de.desy.language.snl.parser.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import de.desy.language.snl.parser.nodes.VariableNode;

public class VariableParser_Test extends TestCase {

	private final String _source = "/*HAllo*/\nprogram sncExample;"
			+ "double v;" + "assign v to \"{user}:aiExample\";"
			+ "monitor v;\n\n\n" + "long l;\n\n" + "%{\n" + "   Embedded C\n"
			+ "}%\n" + "ss ss1 {" + "    state init {" + "	when (delay(0.1)) {"
			+ "	    printf(\"sncExample: Startup delay over\n\");"
			+ "	} state low" + "    }" + " /* Hallo Welt!*" + " ./. */"
			+ "    state low {" + "	    when (v > 50.0) {"
			+ "	        printf(\"sncExample: Changing to high\n\");" + "/* +++"
			+ "*/	    } state high" + "       " + "       when ( delay(1.0) )"
			+ "       {" + "       } state low" + "   }" + "    state high {"
			+ "when (v <= 50.0) {"
			+ "	    printf(\"sncExample: Changing to low\n\");"
			+ "	} state low" + "        when ( delay(1.0) ) {"
			+ "       } state high" + "   }" + "}";

	@Test
	public void testFindNextCharSequence() {
		final VariableParser parser = new VariableParser();

		// double v
		parser.findNext(this._source);
		Assert.assertTrue(parser.hasFoundElement());
		Assert.assertEquals("double v;", parser.getLastFoundStatement());
		VariableNode lastFoundAsNode = parser.getLastFoundAsNode();
		Assert.assertEquals(VariableNode.class, lastFoundAsNode.getClass());
		Assert.assertEquals("v", lastFoundAsNode.getSourceIdentifier());
		Assert.assertEquals("double", lastFoundAsNode.getTypeName());
		Assert.assertTrue(lastFoundAsNode.hasOffsets());
		Assert.assertEquals(29, lastFoundAsNode.getStatementStartOffset());
		Assert.assertEquals(37, lastFoundAsNode.getStatementEndOffset());

		Assert.assertEquals(parser.getEndOffsetLastFound(), lastFoundAsNode
				.getStatementEndOffset());

		// long l
		parser.findNext(this._source, parser.getEndOffsetLastFound());
		Assert.assertTrue(parser.hasFoundElement());
		Assert.assertEquals("long l;", parser.getLastFoundStatement());
		lastFoundAsNode = parser.getLastFoundAsNode();
		Assert.assertEquals(VariableNode.class, lastFoundAsNode.getClass());
		Assert.assertEquals("l", lastFoundAsNode.getSourceIdentifier());
		Assert.assertEquals("long", lastFoundAsNode.getTypeName());
		Assert.assertTrue(lastFoundAsNode.hasOffsets());
		Assert.assertEquals(82, lastFoundAsNode.getStatementStartOffset());
		Assert.assertEquals(88, lastFoundAsNode.getStatementEndOffset());
	}

}
