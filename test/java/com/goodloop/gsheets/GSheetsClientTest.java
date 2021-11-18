package com.goodloop.gsheets;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.api.services.sheets.v4.model.Spreadsheet;

/**
 * @tested {@link GSheetsClient}
 * @author daniel
 *
 */
public class GSheetsClientTest {

	@Test
	public void testGetSheet() throws Exception {        
        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";        
        GSheetsClient sq = new GSheetsClient();
        Spreadsheet s = sq.getSheet(spreadsheetId);
        System.out.println(s);
        assert s != null;

	}
	

	@Test
	public void testGetData() throws Exception {        
        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";        
        GSheetsClient sq = new GSheetsClient();
        List<List<Object>> data = sq.getData(spreadsheetId, "A:A", null);
        assert ! data.isEmpty();

	}
	
	/**
	 * Just a test sheet on Google
	 * https://docs.google.com/spreadsheets/d/1vksonmI0OWqshxPb7rNd5cKAMUbXZDwc1E0U6unSGw0/edit#gid=0
	 */
	public static String sid = "1vksonmI0OWqshxPb7rNd5cKAMUbXZDwc1E0U6unSGw0";
	
//	@Test // fills the drive with junk 'cos I can't find the delete option
	public void testCreateSheet() throws Exception {
        GSheetsClient sq = new GSheetsClient();        
        Spreadsheet s2 = sq.createSheet("Test testCreateSheet");
        String sid = s2.getSpreadsheetId();
        System.out.println(sid);
        assert sid != null;
        
//        sq.todoUpdateSheet(sid);
	}
	

//	@Test // fills the drive with junk 'cos I can't find the delete option
	public void testCreateSheetTitle() throws Exception {
        GSheetsClient sq = new GSheetsClient();
        Spreadsheet s2 = sq.createSheet("Foo Test testCreateSheetTitle");
        String sid = s2.getSpreadsheetId();
        System.out.println(sid);
        assert sid != null;
	}

	@Test
	public void testUpdateValues() throws GeneralSecurityException, IOException {
		GSheetsClient sq = new GSheetsClient();
		List<List<Object>> vs = Arrays.asList(
			Arrays.asList("row","jan","feb","mar"),
			Arrays.asList("Alice", "apples in January", "avocados in Feb", 0),
			Arrays.asList("Bob", "berries in Jan", "blueberries in Feb", 100)
		);
		sq.updateValues(sid, vs);
	}

	

	@Test
	public void testGetBase26() throws GeneralSecurityException, IOException {
		GSheetsClient sq = new GSheetsClient();
		String a = sq.getBase26(0);
		String z = sq.getBase26(25);
		String aa = sq.getBase26(26);
		String ab = sq.getBase26(27);
		String d = sq.getBase26(123);
		assert a.equals("A") : a;
		assert ab.equals("AB");
		assert z.equals("Z");
	}

}
