/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2003-2010 The eXist Project
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
 *  $Id: Account.java 12494 2010-08-21 12:40:10Z shabanovd $
 */
package org.exist.security;

import java.util.Set;

import org.exist.security.realm.Realm;
import org.exist.xmldb.XmldbURI;

@Deprecated //use Account
public interface User extends Principal {

	public final static int PLAIN_ENCODING = 0;
	public final static int SIMPLE_MD5_ENCODING = 1;
	public final static int MD5_ENCODING = 2;

	/**
	 * Add the user to a group
	 *
	 * @param  group  The feature to be added to the Group attribute
	 */
	public Group addGroup(String name) throws PermissionDeniedException;;

	/**
	 * Add the user to a group
	 *
	 * @param  group  The feature to be added to the Group attribute
	 */
	public Group addGroup(Group group) throws PermissionDeniedException;;

	/**
	 *  Remove the user to a group
	 *  Added by {Marco.Tampucci and Massimo.Martinelli}@isti.cnr.it  
	 *
	 *@param  group  The feature to be removed to the Group attribute
	 */
	public void remGroup(String group);

	/**
	 *  Get all groups this user belongs to
	 *
	 *@return    The groups value
	 */
	public String[] getGroups();

	public boolean hasDbaRole();

	/**
	 *  Get the primary group this user belongs to
	 *
	 *@return    The primaryGroup value
	 */
	public String getPrimaryGroup();
	public Group getDefaultGroup();

	/**
	 *  Is the user a member of group?
	 *
	 *@param  group  Description of the Parameter
	 *@return        Description of the Return Value
	 */
	public boolean hasGroup(String group);

	/**
	 *  Sets the password attribute of the User object
	 *
	 * @param  passwd  The new password value
	 */
	public void setPassword(String passwd);

	public void setHome(XmldbURI homeCollection);

	public XmldbURI getHome();

	public Realm getRealm();

	/**
	 * Get the user's password
	 * 
	 * @return Description of the Return Value
	 * @deprecated
	 */
	public String getPassword();

	@Deprecated
	public String getDigestPassword();

	@Deprecated
	public void setGroups(String[] groups);
	
    /**
     * Add a named attribute.
     *
     * @param name
     * @param value
     */
	public void setAttribute(String name, Object value);

    /**
     * Get the named attribute value.
     *
     * @param name The String that is the name of the attribute.
     * @return The value associated with the name or null if no value is associated with the name.
     */
	public Object getAttribute(String name);

    /**
     * Returns the set of attributes names.
     *
     * @return the Set of attribute names.
     */
    public Set<String> getAttributeNames();
    
    /**
     * Returns the person full name or account name.
     *
     * @return the person full name or account name
     */
    String getUsername();

    /**
     * Indicates whether the account has expired. Authentication on an expired account is not possible.
     *
     * @return <code>true</code> if the account is valid (ie non-expired), <code>false</code> if no longer valid (ie expired)
     */
    boolean isAccountNonExpired();

    /**
     * Indicates whether the account is locked or unlocked. Authentication on a locked account is not possible.
     *
     * @return <code>true</code> if the account is not locked, <code>false</code> otherwise
     */
    boolean isAccountNonLocked();

    /**
     * Indicates whether the account's credentials has expired. Expired credentials prevent authentication.
     *
     * @return <code>true</code> if the account's credentials are valid (ie non-expired), <code>false</code> if no longer valid (ie expired)
     */
    boolean isCredentialsNonExpired();

    /**
     * Indicates whether the account is enabled or disabled. Authentication on a disabled account is not possible.
     *
     * @return <code>true</code> if the account is enabled, <code>false</code> otherwise
     */
    boolean isEnabled();

}