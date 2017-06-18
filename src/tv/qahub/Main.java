
package tv.qahub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
/**
 * 
 * @author jun
 */
public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("entering main(String[])");
			logger.debug("args: " + args);
		}
		
		FileInputStream fis = null;
		try {
			// 設定ファイルパス取得
			if (args == null || args.length == 0) {
				// 引数にパスを設定して下さい。
				logger.debug("This argument is null.");
				
			}
			Properties props = new Properties();
			File pFile = new File(args[0]);
			if (pFile.exists()) {
				fis = new FileInputStream(new File(args[0]));
				props.load(fis);
			} else {
				logger.debug("ファイルが見つからない為、デフォルトファイルを読み込みます。");
				InputStream is = new Main().getClass().getResourceAsStream("../../../../config.properties");
				props.load(is);
			}
			
			Process<ProcessExecutor> proc = new Process<ProcessExecutor>(props);
			// リスナー登録
//			proc.addListner(new ProcessExecutor() {
//				public void execute(Object...objects) {
//					System.out.println("test");
//				}
//			});
			// データベースバックアップリスナー
			proc.addListner(new BackUpDb());
			// ファイルバックアップリスナー
			proc.addListner(new CopyDirectory());
			// リスナー通知
			proc.notifyListener();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BackUpException e) {
			e.printStackTrace();
		} finally {			
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("exiting main()");
		}

	}
	// Key of properties.
	/** データベース接続URL */
	public static final String PROPS_KEY_DATABASE_URL = "database.url";
	/** データベース接続ユーザ */
	public static final String PROPS_KEY_DATABASE_USER = "database.user";
	/** データベース接続パスワード */
	public static final String PROPS_KEY_DATABASE_PASSWORD = "database.password";
	/** データベース名 */
	public static final String PROPS_KEY_DATABASE_NAME = "database.name";
	/** データベースバックアップパス */
	public static final String PROPS_KEY_DATABASE_BACKUP_PATH = "database.backup.path";
	/** コピー元ディレクトリ名 */
	public static final String PROPS_KEY_COPY_FROM = "copy_from.path";
	/** コピー先ディレクトリ名 */
	public static final String PROPS_KEY_COPY_TO ="copy_to.path";
	/** 圧縮するかどうか */
	public static final String PROPS_KEY_COMPRESS = "compress";
}

