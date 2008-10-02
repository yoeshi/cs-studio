package org.csstudio.utility.ldapUpdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.sound.sampled.Line;

// ??? import org.csstudio.alarm.treeView.model.ObjectClass;
import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.utility.ldap.engine.Engine;
import org.csstudio.utility.ldapUpdater.model.DataModel;
import org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class UpdateComparator {

	private DataModel _model;

	private IPreferencesService _prefs;
	
	public UpdateComparator(DataModel model) {
		this._model = model;
	    _prefs = Platform.getPreferencesService();
	}

	public void compareLDAPWithIOC() {
		int ind = 0;
		Boolean _recordsWritten=false;
		
//		List<String> bootedIocNames = new ArrayList<String>();
//		List<String> obsoleteIocNames = new ArrayList<String>();
//		List<String> newIocNames = new ArrayList<String>();

		// for (IOC ioc : _model.getIocList()) {
		// if (_model.getHistoryMap().get(ioc.getName()) != null ) {
		// _model.getLdapList().add(ioc.getName());
		// System.out.println(ioc+" added to LdapList");
		// ReadLdapRecordNames recordReader=new ReadLdapRecordNames(_model);
		// recordReader.readLdapRecs(ioc);
		// while(_model.getLdapRecordNames()==null){}
		// for (String recordName : _model.getLdapRecordNames()) {
		// CentralLogger.getInstance().info(this, "RecordName: "+recordName);
		// }
		// // _model.getLdapRecordNames().add(ioc.getName());
		// } else {
		// _model.getNewIocNames().add(ioc.getName());
		// System.out.println(ioc+" added to NewIocNames, must be written to
		// ldap completely");
		// }
		// }

		for (IOC ioc : _model.getIocList()) {
			if (_model.getHistoryMap().get(ioc.getName()) != null) {
				List<String> recordNames = ioc.getIocRecordNames();
				for (String recordName : recordNames) {
					ind = (recordName.indexOf("+")) + (recordName.indexOf("/"));
					if (ind < 0) {
						InLdap inLdap = new InLdap();
						_recordsWritten=false;
						if (!inLdap.existRecord(ioc, recordName)) {
							// System.out.println(recordName);
							DirContext directory = Engine.getInstance().getLdapDirContext();
							Formatter f = new Formatter();
 							f.format("eren=%s, econ=%s, ecom=EPICS-IOC, efan=%s, ou=EpicsControls",
										recordName, ioc.getName(), ioc.getGroup());
							Attributes afe = attributesForEntry("epicsRecord", "eren", recordName);
							try {
								directory.bind(f.toString(), null, afe); // = Record schreiben
								CentralLogger.getInstance().info( this," Record geschrieben!" + ioc.getName()+ " - " + recordName);
							} catch (NamingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ioc.set_mustWriteIOCToHistory(true);
						}
					}
				}
				if ( _recordsWritten ) {AppendLineToHistfile(ioc.getName()); }
			} else {
				_model.getNewIocNames().add(ioc.getName());
//				System.out.println(ioc + " added to NewIocNames, try to write this IOC data to LDAP completely");
		        CentralLogger.getInstance().info(this, ioc + " added to NewIocNames, try to write this IOC data to LDAP completely");
//				System.out.println();
				// ^ hier kommt das programm vorbei,
				// wenn der IOC in iocListFile (=IOCpathes) steht und
				// wenn der IOC NICHT in der history-liste steht.
				// -------------------------------------------------------------------------
				// erst neuen IOC in LDAP anlegen
//				System.out.println(ioc.getName());
				CentralLogger.getInstance().info( this, "New ioc : " + ioc.getName());
				DirContext directory = Engine.getInstance().getLdapDirContext();
				Formatter f = new Formatter();
				f.format("econ=%s, ecom=EPICS-IOC, efan=%s, ou=EpicsControls", ioc.getName(), ioc.getGroup());
				Attributes afe = attributesForEntry("epicsController", "econ", ioc.getName());
				try {
					directory.bind(f.toString(), null, afe); // = iocNamen schreiben
					CentralLogger.getInstance().info(this, "iocName geschrieben : " + ioc);
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// -------------------------------------------------------------------------
				// dann alle recordNames dieses IOC in LDAP anlegen
				List<String> recordNames2 = ioc.getIocRecordNames();
				for (String recordName2 : recordNames2) {
					ind = (recordName2.indexOf("+"))
							+ (recordName2.indexOf("/"));
					if (ind < 0) {
						InLdap inLdap = new InLdap();
						if (!inLdap.existRecord(ioc, recordName2)) {
							// System.out.println(recordName2);
							DirContext directory2 = Engine.getInstance().getLdapDirContext();
							Formatter f2 = new Formatter();
							f2.format("eren=%s, econ=%s, ecom=EPICS-IOC, efan=%s, ou=EpicsControls",
											recordName2, ioc.getName(), ioc.getGroup());
							Attributes afe2 = attributesForEntry("epicsRecord","eren", recordName2);
							try {
								directory2.bind(f2.toString(), null, afe2); // = ioc records schreiben
								CentralLogger.getInstance().info(this,
										"Record geschrieben!" + ioc.getName() + " - " + recordName2);
							} catch (NamingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						ioc.set_mustWriteIOCToHistory(true);
					}
				}
			}
			if ( ioc.is_mustWriteIOCToHistory()) { AppendLineToHistfile(ioc.getName()); }
		}

		// _model.setBootedIocNames(bootedIocNames);
		// _model.setObsoleteIocNames(obsoleteIocNames);
		// _model.setNewIocNames(newIocNames);

		// for (IOC ioc : _model.getIocList()) {
		// for (String ldapIocString : _model.getLdapList()) {
		// String[] temp = ldapIocString.split("[=,]");
		// if (temp.length > 1) {
		// ldapIocString = temp[1];
		// }
		//
		// boolean ldapObsolete = true;
		// if (_model.getHistoryMap().get(ldapIocString) != null) {
		// if (((GregorianCalendar) ioc.getDateTime()).getTimeInMillis() >
		// _model.getHistoryMap().get(ldapIocString)) {
		// if (ldapIocString.startsWith("econ=")) {
		// String ldapIocName = ldapIocString.split("[,=]")[1];
		// if (ioc.getName().equals(ldapIocName)) {
		// ioc.setMustAdd2Ldap(false);
		// bootedIocNames.add(ldapIocString);
		// System.out.println(ioc + " : data already present in LDAP");
		// } else
		// if (ldapIocName.equals("") ) {
		// newIocNames.add(ioc.getName());
		// System.out.println(ioc + " : NEW - not yet present in LDAP");
		// }
		// else
		// {
		// System.out.println(ioc + " : LDAP data are actual");
		// }
		// ldapObsolete = false;
		// }
		// }
		// }
		// if (ldapObsolete) {
		// // todo :
		// // wenn keine LdapVariable namens xxx mit Wert (Datum) vorhanden,
		// // dann in ldap variable setzen : xxx="disabled since $today"
		// // sonst muss der wert von xxx gelesen und ausgegeben werden
		// // ( die LDAP-Variable xxx will mc in die LDAP configuration data
		// eintragen )
		//					
		// System.out.println(ioc + " : no more active since ...");
		// obsoleteIocNames.add(ldapIocString);
		// }
		// }
		// }
		//
		// _model.setBootedIocNames(bootedIocNames);
		// _model.setObsoleteIocNames(obsoleteIocNames);
		// _model.setNewIocNames(newIocNames);
	}

	/**
	 * Returns the attributes for a new entry with the given object class and
	 * name.
	 * 
	 * @param objectClass
	 *            the object class of the new entry.
	 * @param name
	 *            the name of the new entry.
	 * @return the attributes for the new entry.
	 */
	private Attributes attributesForEntry(final String objectClass,
			String rdnAttr, final String name) {
		BasicAttributes result = new BasicAttributes();
		result.put("objectClass", objectClass);
		result.put(rdnAttr, name);
		// result.put("epicsCssType", objectClass.getCssType());
		return result;
	}

	/**
	 * append a line to the history file.
	 * 
	 * @param iocname
	 */
	private void AppendLineToHistfile(final String iocname) {
//		String _crString;
		try {
			FileWriter fw = new FileWriter(
					_prefs.getString(Activator.getDefault().getPluginId(),
		    	    		LdapUpdaterPreferenceConstants.LDAP_HIST_PATH, "", null) + "history.dat", true);
			long now = System.currentTimeMillis();
			
			now = now / 1000;
			
			String _iocname = iocname;
			do  { _iocname = _iocname.concat ( " " ); } while (_iocname.length() < 16);
            
//			_crString = System.getProperty("line.separator"); // should be used by the statement below : "fw.append" !! not working !!!
//			fw.append ( _iocname + "xxx     " + now + _crString ); 
			fw.append ( _iocname + "xxx     " + now + System.getProperty("line.separator" ) ); 
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
