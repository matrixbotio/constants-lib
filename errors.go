package constants

import (
	"fmt"
	"strings"

	"github.com/go-stack/stack"
)

const errStackJoin = "\n    at "

type APIError struct {
	Message string `json:"message"`
	Code    int    `json:"code"`
	Name    string `json:"name"`
	Stack   string `json:"-"`
}

func (e APIError) Error() string {
	return fmt.Sprintf("name: %s; code: %d; message: %s", e.Name, e.Code, e.Message)
}

func getErrors(url string) map[string]*APIError {
	storage := make(map[string]*APIError)
	getJSON(url, &storage)
	return storage
}

func getStack() string {
	rt := stack.Trace().TrimRuntime()
	rt = rt[3:]
	str := fmt.Sprintf("%#v", rt)
	str = str[1:len(str)-1]
	arr := strings.Split(str, " ")
	return errStackJoin[1:] + strings.Join(arr, errStackJoin)
}

func err(code int, name string, msg string) *APIError {
	return &APIError{
		Message: msg,
		Code:    code,
		Name:    name,
		Stack:   getStack(),
	}
}

var errors = getErrors("https://raw.githubusercontent.com/matrixbotio/constants/master/errors/errors.json")

func Error(name string, message ...string) *APIError {
	if res, exists := errors[name]; exists {
		if len(message) > 0 {
			return err(res.Code, res.Name, message[0])
		}
		return err(res.Code, res.Name, res.Message)
	}
	return err(-1, "ERR_UNKNOWN", "Cannot get error named "+name+"; original message: "+message[0])
}
