package h;

import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import dk.webbies.tscreate.paser.AST.AstNode;

public class Helper {
    private static final boolean DEBUG = true;
	public static final void printDebug(String m) {
		printDebug(null, m);
	}
	public  static final void printDebug(String prompt, String msg) {
		if (prompt == null) prompt = "HDEB";
		if (DEBUG) {
			System.out.println("[ "+ prompt +" ]" + msg);
		}
	}
	public static String getText(AstNode astnode) {
		if (astnode == null) return "AST_NULL";
		if (astnode.location == null) return "LOC_NULL";
		StringBuilder ret = new StringBuilder();
		ret.append("clazz: " + astnode.getClass().getName() + "\n");

		SourcePosition start = astnode.location.start;
		SourcePosition end = astnode.location.end;
		//assert (start.source.contents.equals(end.source.contents));
		String contents = start.source.contents;

		for (int i = start.offset; i < end.offset; i++) {
			ret.append(contents.charAt(i));
		}
		return ret.toString();
	}
	
}
