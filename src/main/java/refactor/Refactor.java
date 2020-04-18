package refactor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;


public class Refactor {

	private List<String> REGEXP = new ArrayList<String>();
	private List<String> REGEXP_R = new ArrayList<String>();
	private List<String> NAME_REGEXP = new ArrayList<String>();
	private List<String> NAME_REGEXP_R = new ArrayList<String>();
	private  String OUT = null;
	private  String IN = null;

	public Refactor(String[] args) throws Exception {
		parseParams(args);
	}

	private void parseParams(String[] args) throws Exception {
		if(args.length<6) printUsage();

	   for (int i=0;i<args.length;i++) {
			if("-in".equals(args[i])){
				IN = args[++i];
			}else if("-out".equals(args[i])){
				OUT = args[++i];
			}else if("-replace".equals(args[i])){
				REGEXP.add(args[++i]);
				REGEXP_R.add(args[++i]);
			}else if("-replaceName".equals(args[i])){
				NAME_REGEXP.add(args[++i]);
				NAME_REGEXP_R.add(args[++i]);
			}else{
				printUsage();
			}
		}
		
	}
   
   
	private void printUsage() throws Exception {
		throw new Exception("Errores en los argumentos. Uso:\n java -jar <nombrejar>.jar \n "
				+ "[-in <path to the dir>\n "
				+ "[-out <path to dir>]"	
				+ "[-replace <multiline regexp> <multiline replacement>]"
				+ "[-replaceName <filename regexp> <filename replacement>]"
				+ "");
	}
	
	public static void main(String[] args) {

		Refactor r;
		try {
			r = new Refactor(args);
			r.run(r.IN,r.OUT);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	private void run(String in, String out) throws IOException {
		File inDir = new File(in);
		File outDir = new File(out);
		
		outDir.mkdirs();
		
		File[] listFiles = inDir.listFiles();
		
		for (File file : listFiles) {
			File actualFileName=file;
			for (int i=0;i<NAME_REGEXP.size();i++){
				File f=null;
					f = new File(outDir,actualFileName.getName().replaceAll(NAME_REGEXP.get(i), NAME_REGEXP_R.get(i)));
					if(actualFileName.isDirectory()){
						f.mkdirs();
						run(actualFileName.getAbsolutePath(), f.getAbsolutePath());
					}else{
						f.createNewFile();
						replaceToFile(actualFileName,f,REGEXP,REGEXP_R);
					}
				if(!file.getName().equals(actualFileName.getName())) actualFileName.delete();
				actualFileName=f;

			}
		
		
		}

	}
	
	 public String detect(byte[] data, String hint) { 
	        CharsetDetector detector = new CharsetDetector(); 
	        if (hint != null) { 
	            detector.setDeclaredEncoding(hint); 
	        } 
	        detector.setText(data); 
	        CharsetMatch match = detector.detect(); 
	        if(match!=null)
	        	return match.getName();
	        else 
	        	return null;
	    }

	private void replaceToFile(File actualFileName, File f, List<String> regex, List<String> replacement) throws IOException {
		String encoding = null;

		FileOutputStream fos=new FileOutputStream(f);
		FileInputStream fis=new FileInputStream(actualFileName);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		byte[] buf=new byte[1024];
		int readed=-1;
		try{
			while((readed=fis.read(buf))!=-1){
				baos.write(buf,0,readed);
			}
			encoding=detect(baos.toByteArray(), null);
			if(encoding==null){
				System.out.println(actualFileName+" raw copy.");
				fos.write(baos.toByteArray());
			}else{
				System.out.println(actualFileName+" translate using charset "+encoding);
			
				String str = baos.toString(encoding);
				for(int i=0;i<regex.size();i++){
					str=str.replaceAll(regex.get(i), replacement.get(i));
				}
				
				fos.write(str.getBytes(encoding));
			}
		}finally{
			try{
				if(fis!=null) fis.close();
			}finally{
				if(fos!=null) fos.close();
			}
		}
	}
}