package at.mtel.dms.file;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FileVisitor extends SimpleFileVisitor<Path> {

	public FileVisitor(String extension) {
		this.exstension = extension.toLowerCase();
	}

	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
		if (attr.isRegularFile()) {
			String parentName = file.getParent().getFileName().toString();
			if (file.getFileName().toString().toLowerCase().endsWith(exstension))
				paths.add(file);
		}
		return CONTINUE;
	}

	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		return CONTINUE;
	}

	public List<Path> getPaths() {
		return paths;
	}

	private String exstension = ".ext";
	private List<Path> paths = new ArrayList<Path>();
}