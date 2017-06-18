package tv.qahub;

public interface ProcessExecutor {

	public void execute(Object...objects) throws BackUpException;
}
