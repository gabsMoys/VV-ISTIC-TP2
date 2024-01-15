package com.vv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.javaparser.utils.SourceRoot;

public class Main {

	public static void main(String[] args) throws IOException {
		/*
		 * if (args.length == 0) {
		 * System.err.println("Should provide the path to the source code");
		 * System.exit(1); }
		 * 
		 * File file = new File(args[0]); if (!file.exists() || !file.isDirectory() ||
		 * !file.canRead()) {
		 * System.err.println("Provide a path to an existing readable directory");
		 * System.exit(2); }
		 */
		String outPath = "/home/gaby/VV/repoTP2/VV-ISTIC-TP2/noGetter.txt";
		File file = new File("/home/gaby/eclipse-workspace/VV_TP2_JP/src/main/java/com/vv/testClasse");
		PrintWriter out = new PrintWriter(new FileOutputStream(new File(outPath)));
		SourceRoot root = new SourceRoot(file.toPath());
		try {
			// PublicElementsPrinter printer = new PublicElementsPrinter();
			PrivateFieldWithoutPublicGetterPrinter visiteur = new PrivateFieldWithoutPublicGetterPrinter(out);
			root.parse("", (localPath, absolutePath, result) -> {
				result.ifSuccessful(unit -> unit.accept(visiteur, null));
				return SourceRoot.Callback.Result.DONT_SAVE;
			});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.flush();// probablement redondant, le close fait sans doute un flush
				out.close();
			}

		}
	}

}
