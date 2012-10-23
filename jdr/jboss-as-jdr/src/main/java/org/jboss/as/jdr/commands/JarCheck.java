/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.jdr.commands;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.jboss.as.jdr.JdrLogger.ROOT_LOGGER;

public class JarCheck extends JdrCommand {

    StringBuilder buffer;

    @Override
    public void execute() throws Exception {
        this.buffer = new StringBuilder();
        walk(new java.io.File(this.env.getJbossHome()));
        this.env.getZip().add(this.buffer.toString(), "jarcheck.txt");
    }

    private void walk(File root) throws NoSuchAlgorithmException {
        for(File f : root.listFiles()) {
            if(f.isDirectory()) {
                walk(f);
            }
            else {
                check(f);
            }
        }
    }

    private void check(File f) throws NoSuchAlgorithmException {
        try {
            MessageDigest alg = MessageDigest.getInstance("md5");
            JarFile jf = new JarFile(f);
            FileInputStream fis = new FileInputStream(f);
            byte [] buffer = new byte[(int) f.length()];
            fis.read(buffer);
            alg.update(buffer);
            String sum = new BigInteger(1, alg.digest()).toString(16);
            this.buffer.append(
                    f.getCanonicalPath().replace(this.env.getJbossHome(), "JBOSSHOME") + "\n"
                    + sum + "\n"
                    + getManifestString(jf) + "===");
        }
        catch( java.util.zip.ZipException ze ) {
            // skip
        }
        catch( java.io.FileNotFoundException fnfe ) {
            ROOT_LOGGER.debug(fnfe);
        }
        catch( java.io.IOException ioe ) {
            ROOT_LOGGER.debug(ioe);
        }
    }

    private String getManifestString(JarFile jf) throws java.io.IOException {
        StringBuilder buffer = new StringBuilder();
        try {
            ZipEntry manifest = jf.getEntry("META-INF/MANIFEST.MF");
            InputStream manifest_is = jf.getInputStream(manifest);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(manifest_is));
            String line = reader.readLine();
            while( line.length() != 0 ) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }
            return buffer.toString();
        } catch (NullPointerException npe) {
            ROOT_LOGGER.tracef("no MANIFEST present");
            return "";
        }
    }
}
