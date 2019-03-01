package xyz.fz.docdoc.helper.model;

public class DocResult {
    private boolean success;

    private String message;

    private DocLocation data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DocLocation getData() {
        return data;
    }

    public void setData(DocLocation data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DocResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
