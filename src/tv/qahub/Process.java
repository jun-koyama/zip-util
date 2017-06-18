/**
 * 
 */
package tv.qahub;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;


import org.apache.log4j.Logger;

/**
 * Observerクラス
 *
 * @param <T>
 */
public class Process<T extends ProcessExecutor> {

	/**
	 * コンストラクタ
	 * @param props
	 */
	public Process(Properties props) {
		this.props = props;
	}
	
	/**
	 * リスナーを追加します。
	 * @param o
	 */
	public void addListner(T o) {
		observers.add(o);
	}
	
	/**
	 * リスナーを削除します。
	 * @param o
	 */
	public void removeListener(T o) {
		observers.remove(o);
	}
	
	/**
	 * 各クラスに通知します。
	 */
	public void notifyListener() throws BackUpException {
		for (T o : observers) {
			logger.debug(o.getClass().getName() + " start.");
			o.execute(props);
			logger.debug(o.getClass().getName() + " end.");
		}
	}
	/** Observer */
	private final List<T> observers = new CopyOnWriteArrayList<T>();
	/** 設定ファイル */
	private Properties props = null;
	private static Logger logger = Logger.getLogger(Process.class);

}
