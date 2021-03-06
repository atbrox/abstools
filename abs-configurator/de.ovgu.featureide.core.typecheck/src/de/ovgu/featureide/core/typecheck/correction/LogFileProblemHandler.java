/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2011  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.core.typecheck.correction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import de.ovgu.featureide.core.typecheck.check.CheckProblem;

/**
 * Not working yet
 * 
 * @author Soenke Holthusen
 */
public class LogFileProblemHandler implements IProblemHandler {
    String log_file;

    /**
     * 
     */
    public LogFileProblemHandler(String log_file) {
	this.log_file = log_file;
	File log = new File(log_file);
	if(!log.exists()){
	    try {
		log.createNewFile();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ovgu.featureide.core.typecheck.correction.IProblemHandler#handleProblems
     * (java.util.Collection)
     */
    @Override
    public void handleProblems(Collection<CheckProblem> problems) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ovgu.featureide.core.typecheck.correction.IProblemHandler#log(java
     * .lang.String)
     */
    @Override
    public void log(String message) {
	try {
	    new PrintStream(new FileOutputStream(log_file)).println(message);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
