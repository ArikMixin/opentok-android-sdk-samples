package io.slatch.app.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class StateData<T> {

	public enum DataStatus {
		CREATED,
		SUCCESS,
		ERROR_LOGIC,
		ERROR_COM,
		LOADING,
		COMPLETE
	}

	@NonNull
	private DataStatus status;

	@Nullable
	private T data;

	@Nullable
	private String  error_logic;

	@Nullable
	private Throwable error_com;

	public StateData() {
		this.status = DataStatus.CREATED;
		this.data = null;
		this.error_com = null;
		this.error_logic = null;
	}

	public StateData<T> loading() {
		this.status = DataStatus.LOADING;
		this.data = null;
		this.error_com = null;
		this.error_logic = null;
		return this;
	}

	public StateData<T> success(@NonNull T data) {
		this.status = DataStatus.SUCCESS;
		this.data = data;
		this.error_com = null;
		this.error_logic = null;
		return this;
	}

	public StateData<T> errorLogic(@NonNull String error) {
		this.status = DataStatus.ERROR_LOGIC;
		this.data = null;
		this.error_com = null;
		this.error_logic = error;
		return this;
	}

	public StateData<T> errorComm(@NonNull Throwable error) {
		this.status = DataStatus.ERROR_COM;
		this.data = null;
		this.error_com = error;
		this.error_logic = null;
		return this;
	}

	public StateData<T> complete() {
		this.status = DataStatus.COMPLETE;
		this.error_com = null;
		this.error_logic = null;
		return this;
	}

	@NonNull
	public DataStatus getStatus() {
		return status;
	}

	@Nullable
	public T getData() {
		return data;
	}

	@Nullable
	public Throwable getErrorCom() {
		return error_com;
	}

	@Nullable
	public String getErrorLogic() {
		return error_logic;
	}


	public boolean isSuccess(){
		return (status == DataStatus.SUCCESS);
	}

	public boolean isErrorComm(){
		return (status == DataStatus.ERROR_COM);
	}

	public boolean isErrorLogic(){
		return (status == DataStatus.ERROR_LOGIC);
	}

}
