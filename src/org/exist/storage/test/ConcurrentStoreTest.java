/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.storage.test;

import java.io.File;
import java.util.Iterator;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.dom.DocumentImpl;
import org.exist.security.SecurityManager;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.Configuration;
import org.exist.util.XMLFilenameFilter;
import org.exist.xmldb.test.concurrent.DBUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConcurrentStoreTest extends TestCase {

    public static void main(String[] args) {
        TestRunner.run(ConcurrentStoreTest.class);
    }
    
    private static String directory = "/home/wolf/xml/shakespeare";
    
    private static File dir = new File(directory);
    
    private BrokerPool pool;
    private Collection test, test2;
    
    public synchronized void testStore() throws Exception {
        BrokerPool.FORCE_CORRUPTION = true;
        pool = startDB();
        setupCollections();
        
        Thread t1 = new StoreThread1();
        t1.start();
        
        wait(8000);
        
        Thread t2 = new StoreThread2();
        t2.start();
        
        t1.join();
        t2.join();
    }
    
    public void testRead() throws Exception {
        BrokerPool.FORCE_CORRUPTION = false;
        pool = startDB();
        
        DBBroker broker = null;
        try {
            broker = pool.get(SecurityManager.SYSTEM_USER);
            test = broker.getCollection("/db/test/test1");
            assertNotNull(test);
            test2 = broker.getCollection("/db/test/test2");
            assertNotNull(test2);
            System.out.println("Contents of collection " + test.getName() + ":");
            for (Iterator i = test.iterator(broker); i.hasNext(); ) {
                DocumentImpl next = (DocumentImpl) i.next();
                System.out.println("- " + next.getName());
            }
        } finally {
            pool.release(broker);
        }
    }
    
    protected void setupCollections() throws Exception {
        DBBroker broker = null;
        TransactionManager transact = pool.getTransactionManager();
        Txn transaction = transact.beginTransaction();
        try {
            broker = pool.get(SecurityManager.SYSTEM_USER);
            
            System.out.println("Transaction started ...");
            
            Collection root = broker.getOrCreateCollection(transaction, "/db/test");
            broker.saveCollection(transaction, root);
            
            test = broker.getOrCreateCollection(transaction, "/db/test/test1");
            broker.saveCollection(transaction, test);
            
            test2 = broker.getOrCreateCollection(transaction, "/db/test/test2");
            broker.saveCollection(transaction, test2);
            
            transact.commit(transaction);
        } catch (Exception e) {
            transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            pool.release(broker);
        }
    }
    
    protected BrokerPool startDB() throws Exception {
        String home, file = "conf.xml";
        home = System.getProperty("exist.home");
        if (home == null)
            home = System.getProperty("user.dir");
        try {
            Configuration config = new Configuration(file, home);
            BrokerPool.configure(1, 5, config);
            return BrokerPool.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return null;
    }

    protected void tearDown() throws Exception {
        BrokerPool.stopAll(false);
    }
    
    class StoreThread1 extends Thread {
        
        public void run() {
            DBBroker broker = null;
            try {
                broker = pool.get(SecurityManager.SYSTEM_USER);
                
                TransactionManager transact = pool.getTransactionManager();
                Txn transaction = transact.beginTransaction();
                
                System.out.println("Transaction started ...");
                XMLFilenameFilter filter = new XMLFilenameFilter();
                File files[] = dir.listFiles(filter);
                
                File f;
                IndexInfo info;
                // store some documents into /db/test
                for (int i = 0; i < files.length; i++) {
                    f = files[i];
                    try {
                        info = test.validate(transaction, broker, f.getName(), new InputSource(f.toURI().toASCIIString()));
                        test.store(transaction, broker, info, new InputSource(f.toURI().toASCIIString()), false);
                    } catch (SAXException e) {
                        System.err.println("Error found while parsing document: " + f.getName() + ": " + e.getMessage());
                    }
//                    if (i % 5 == 0) {
//                        transact.commit(transaction);
//                        transaction = transact.beginTransaction();
//                    }
                }
                
                transact.commit(transaction);
                
                transact.getLogManager().flushToLog(true);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                pool.release(broker);
            }
        }
    }
    
    class StoreThread2 extends Thread {
        public void run() {
            DBBroker broker = null;
            try {
                broker = pool.get(SecurityManager.SYSTEM_USER);
                
                TransactionManager transact = pool.getTransactionManager();
                Txn transaction = transact.beginTransaction();
                
                System.out.println("Transaction started ...");
                
                Iterator i = test.iterator(broker);
                DocumentImpl doc = (DocumentImpl)i.next();
                
                System.out.println("\nREMOVING DOCUMENT\n");
                test.removeDocument(transaction, broker, doc.getFileName());
                
                File f = new File(dir + File.separator + "hamlet.xml");
                try {
                    IndexInfo info = test.validate(transaction, broker, "test.xml", new InputSource(f.toURI().toASCIIString()));
                    test.store(transaction, broker, info, new InputSource(f.toURI().toASCIIString()), false);
                } catch (SAXException e) {
                    System.err.println("Error found while parsing document: " + f.getName() + ": " + e.getMessage());
                }
                
                transact.commit(transaction);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                pool.release(broker);
            }
        }
    }
}