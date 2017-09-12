package training.edu.ws;

/**
 * Created by gcoronad on 05/09/2017.
 */

public interface OnTaskListener {
    void OnTaskCompleted(String json);
    void OnTaskError(int code, String message, String error);
}
