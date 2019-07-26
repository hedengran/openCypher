/*
 * Copyright (c) 2015-2019 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools.grammar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.opencypher.grammar.Grammar;
import org.opencypher.tools.antlr.BNFProcessor;
import org.opencypher.tools.antlr.G4Processor;
import org.xml.sax.SAXException;

/** translate between supported grammar serialisations, via the Grammar object model */
public class TranslateGrammar {

	public static void main(String[] args) throws Exception
	{
		List<String> argList =  new ArrayList<>(Arrays.asList(args));
		String inXml  = getArg("x", argList);
		String inG4   = getArg("g", argList);
		String inBnf  = getArg("b", argList);
		String outXml = getArg("X", argList);
		String outG4  = getArg("G", argList);
		String outBnf = getArg("B", argList);
		
		if (argList.size() != 0) {
			usage(args);
			System.exit(1);
		}
		// ought to check for duplicates - just take the first if they're silly
		Grammar grammar = null;
        Grammar.ParserOption[] parserOptions = Grammar.ParserOption.from( System.getProperties() );
		if (inXml != null) {
            grammar = Grammar.parseXML( Paths.get( inXml ), parserOptions );
		} else if ( inG4 != null) {
			G4Processor g4Processor = new G4Processor();
			grammar = g4Processor.processFile(inG4);
		} else if (  inBnf != null) {
			BNFProcessor bnfProcessor = new BNFProcessor();
			grammar = bnfProcessor.processFile(inBnf);
		}
		if (grammar == null) {
			System.err.println("No input grammar was specified");
			usage(args);
			System.exit(1);
		}
		boolean written = false;
		if (outXml != null) {
			OutputStream outStream = outXml.equals(".") ? System.out : new FileOutputStream(outXml);
			Xml.write(grammar,  outStream);
			outStream.close();
			written = true;
		} 
		if (outG4 != null) {
			OutputStream outStream = outG4.equals(".") ? System.out :new FileOutputStream(outG4);
			Antlr4.write(grammar,  outStream);
			outStream.close();
			written = true;
			
		}
		if (outBnf != null) {
			OutputStream outStream = outBnf.equals(".") ? System.out :new FileOutputStream(outBnf);
			SQLBNF.write(grammar,  outStream);
			outStream.close();
			written = true;
			
		}
		if (!written) {
			Xml.write(grammar,  System.out);
		}

	}

	static String getArg(String key, List<String> argList) {
	   	int index = argList.indexOf("-" + key);
    	if (index >= 0) {
    		argList.remove(index);
    		return argList.remove(index);
    	} else {
    		return null;
    	}
	}

	static void usage(String [] args) {
        System.err.println( "Arguments:\n" +
        		"      -x : input xml file path\n" +
        		"      -g : input antlr G4 file path\n" +
        		"      -b : input SQL BNF file path\n" +
        		"      -X : output xml file path\n" +
        		"      -G : output antlr G4 file path or . for sysout\n" +
        		"      -B : output SQL BNF file path or . for sysout\n" +
        		" One input file path must be given or . for sysout\n" +
        		" Zero or more output file paths may be given\n" +
        		" If no output file path is given, the output will go to sysout.");
        
	}
}
