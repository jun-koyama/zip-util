package tv.qahub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class CopyDirectory implements ProcessExecutor {
	private static Logger logger = Logger.getLogger(CopyDirectory.class);

	@Override
	public void execute(Object... objects) throws BackUpException {
		Properties props = (Properties)objects[0];
		try {
			String targetFileName = this.getBackupFilePath(props.getProperty(Main.PROPS_KEY_COPY_TO));
			boolean isCompress = Boolean.parseBoolean(props.getProperty(Main.PROPS_KEY_COMPRESS));
			if (isCompress) {
				this.compress(props.getProperty(Main.PROPS_KEY_COPY_FROM), targetFileName + ".zip");
			} else {
				this.copy(new File(props.getProperty(Main.PROPS_KEY_COPY_FROM))
				, new File(targetFileName));
				
			}
		} catch (IOException e) {
			throw new BackUpException(e);
		}
	}

	/**
	 * バックアップファイルパスの取得
	 * @param path
	 * @return
	 */
	protected String getBackupFilePath(String path) {
		String targetPath = "";
		Calendar cal = Calendar.getInstance();
		int week = cal.get(Calendar.DAY_OF_WEEK);
		targetPath = path + WEEK_MAP.get(new Integer(week));
		File targetDir = new File(targetPath);
		if (!targetDir.exists()) {
			targetDir.mkdir();
		}
		// 以前のファイルを削除
		String[] backupFile = targetDir.list();
		for (String f : backupFile) {
			File file = new File(f);
			file.delete();
		}
		targetPath += System.getProperty("file.separator") + getSysDate("yyyyMMdd");
		return targetPath;
	}
	
	/**
	 * 日付の取得
	 * @param format
	 * @return
	 */
	protected String getSysDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}
	/**
	 * File Copy
	 * @param sourceLocation source
	 * @param targetLocation target
	 * @throws IOException
	 */
    public void copy(File sourceLocation , File targetLocation) throws IOException {
    	logger.debug("this.copy();");
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
            	copy(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
        	BufferedInputStream bis = null;
        	BufferedOutputStream bos = null;
        	try {
        		bis = new BufferedInputStream(new FileInputStream(sourceLocation));
        		bos = new BufferedOutputStream(new FileOutputStream(targetLocation));
	            byte[] buf = new byte[1024];
	            int len;
	            while ((len = bis.read(buf)) > 0) {
	            	bos.write(buf, 0, len);
	            }
        	} finally {
        		if (bis != null) {
        			try {
        				bis.close();
        			} catch (IOException ioe) {}
        		}
        		if (bos != null) {
        			try {
        				bos.close();
        			} catch (IOException ioe) {}
        		}
        	}
        }
    }
    
    /**
     * 指定ディレクトリをZIP形式に圧縮します。
     * @param directoryPath 圧縮対象ディレクトリ(フルパス指定)
     * @return 圧縮ファイル名(フルパス)
     * @throws IOException 入出力関連エラーが発生した場合
     */
    public void compress(String sourceDir, String targetZipFile) throws IOException {
    	ZipOutputStream out = null;
    	try {
	        logger.info("圧縮開始");
	        logger.info("圧縮ディレクトリ=" + sourceDir);
	        // ディレクトリ存在チェック
	        File sourceDirectory = new File(sourceDir);
	        // 指定ディレクトリ直下のファイル一覧を取得
	        List<File> listFiles = Arrays.asList(sourceDirectory.listFiles());
	        logger.info("圧縮ファイル名=" + targetZipFile);
	        out = new ZipOutputStream(new FileOutputStream(targetZipFile));
	        // ディレクトリ自体を書き込む
	        putEntryDirectory(sourceDir, out, sourceDirectory);
	        // ファイルリスト分の圧縮処理
	        compress(sourceDir, out, listFiles);
	        // 出力ストリームを閉じる
	        out.flush();
    	} finally {
    		if (out != null) {
    			try {
    				out.close();
    			} catch (Exception e) {}
    		}
    	}
        logger.info("圧縮終了");
        return;
    }
    /**
     * <pre>
     * ファイル一覧(ディレクトリ含む)をZipOutputStreamに登録します。
     * ディレクトリが存在する場合は再帰的に本メソッドをコールし全てのファイルを登録します。
     * </pre>
     * @param out
     * @param fileList
     * @throws IOException
     */
    private void compress(String sourceDir, ZipOutputStream out, List<File> fileList) throws IOException {
       for (File file : fileList) {
            if (file.isFile()) {
                logger.info("file compress->" + file.getPath());
                // ファイル書き込み
                putEntryFile(sourceDir, out, file);
            } else {
                // ディレクトリ自体を書き込む
                putEntryDirectory(sourceDir, out, file);
                // ディレクトリ内のファイルについては再帰的に本メソッドを処理する
                List<File> inFiles = Arrays.asList(file.listFiles());
                compress(sourceDir, out, inFiles);
            }
        }
    }
    /**
     * <pre>
     * ZipOutputStreamに対してファイルを登録します。
     * </pre>
     * @param out
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void putEntryFile(String sourceDir, ZipOutputStream out, File file) throws FileNotFoundException, IOException {
    	BufferedInputStream in = null;
    	try {
    	byte[] buf = new byte[128];
        in = new BufferedInputStream(new FileInputStream(file));
        // エントリを作成する
        ZipEntry entry = new ZipEntry(getZipEntryName(sourceDir, file.getPath()));
        out.putNextEntry(entry);
        // データ書き込む
        int size;
        while ((size = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, size);
        }
        // エントリと入力ストリームを閉じる
        out.closeEntry();
    	} finally {
    		if (in != null) {
    			try {
    			in.close();
    			} catch (Exception e) {}
    		}
    	}
    }

    /**
     * <pre>
     * ディレクトリを登録します。
     * </pre>
     * @param out
     * @param file
     * @throws IOException
     */
    private void putEntryDirectory(String sourceDir, ZipOutputStream out, File file) throws IOException {
        ZipEntry entry = new ZipEntry(getZipEntryName(sourceDir, file.getPath()) + "/");
        entry.setSize(0);
        out.putNextEntry(entry);
    }
    
    /**
     * <pre>

     * </pre>
     * @param filePath ファイルパス
     * @return ファイル名
     */
    private String getZipEntryName(String targetDir, String filePath) {
        String parantPath = (new File(targetDir)).getParent();
        parantPath = removeSeparator(parantPath); 
        return filePath.substring(parantPath.length() + 1);

    }
    private String removeSeparator(String path) {
    	String separator = System.getProperty("file.separator");
    	if (!path.endsWith(separator)) {
    		return path;
    	}
    	return path.substring(0, path.length() - 1);
    }

    /**
     * 曜日に対応するディレクトリ
     */
    private static final Map<Integer, String> WEEK_MAP = new HashMap<Integer, String>();
    static {
    	WEEK_MAP.put(new Integer(Calendar.SUNDAY), "SUN");
    	WEEK_MAP.put(new Integer(Calendar.MONDAY), "MON");
    	WEEK_MAP.put(new Integer(Calendar.TUESDAY), "TUE");
    	WEEK_MAP.put(new Integer(Calendar.WEDNESDAY), "WED");
    	WEEK_MAP.put(new Integer(Calendar.THURSDAY), "THU");
    	WEEK_MAP.put(new Integer(Calendar.FRIDAY), "FRI");
    	WEEK_MAP.put(new Integer(Calendar.SATURDAY), "SAT");
    }
}
