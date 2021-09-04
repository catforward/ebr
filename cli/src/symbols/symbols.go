package symbols

const (
	// normal words
	ALL  string = "all"
	CONF string = "conf"
	HTTP string = "http"
	ADDR string = "address"
	PORT string = "port"
	// command
	SHOW  string = "show"
	START string = "start"
	ABORT string = "abort"
	// return code
	NORMAL               int = 0
	UNKNOWN_COMMAND      int = 1
	UNIMPLEMENTED_ACTION int = 2
	COMMAND_ERROR        int = 3
	FILE_ERROR           int = 4
	JSON_ERROR           int = 5
	SERVER_ERROR         int = 6
)
