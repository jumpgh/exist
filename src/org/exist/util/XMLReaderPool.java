/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2000-04,  Wolfgang M. Meier (wolfgang@exist-db.org)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *  $Id$
 */
package org.exist.util;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.xml.sax.XMLReader;

/**
 * Maintains a pool of XMLReader objects. The pool is available through
 * {@link BrokerPool#getParserPool()}.
 * 
 * @author wolf
 */
public class XMLReaderPool extends StackObjectPool {

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public XMLReaderPool(PoolableObjectFactory factory, int maxIdle, int initIdleCapacity) {
		super(factory, maxIdle, initIdleCapacity);
	}

	public synchronized XMLReader borrowXMLReader() {
		try {
			return (XMLReader)borrowObject();
		} catch(Exception e) {
			throw new IllegalStateException("error while returning XMLReader: " + e.getMessage());
		}
	}
	
	public synchronized void returnXMLReader(XMLReader reader) {
		try {
			returnObject(reader);
		} catch (Exception e) {
			throw new IllegalStateException("error while returning XMLReader: " + e.getMessage());
		}
	}
}
