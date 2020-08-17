package ink.zfei.domain;

public class Result {




    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private int status;
    private String message;
    private String code;

    public static Result success(String code) {
        Result result = new Result();
        result.setCode(code);
        result.setStatus(0);
        result.setMessage("success");
        return result;
    }

}
