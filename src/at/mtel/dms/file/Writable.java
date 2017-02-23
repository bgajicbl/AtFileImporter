package at.mtel.dms.file;

import java.io.IOException;
import java.text.ParseException;

public interface Writable {
	
	void write() throws ParseException, IOException;
	
}
