package constants

import (
	"io/ioutil"
	"net/http"
	"encoding/json"

	"github.com/go-stack/stack"
)

type APIError struct {
	Message string        `json:"message"`
	Code    int           `json:"code"`
	Name    string        `json:"name"`
	Stack   string        `json:"-"`
}

func get(url string) string {
	resp, err := http.Get(url)
	if err != nil {
		return "{}"
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return "{}"
	}
	return string(body)
}

func getErrors(url string) map[string]*APIError {
	var storage map[string]*APIError
	json.Unmarshal([]byte(get(url)), &storage)
	return storage
}

func err(code int, name string, msg string) *APIError {
	return &APIError{
		Message: msg,
		Code:    code,
		Name:    name,
		Stack:   stack.Trace().TrimRuntime().String(),
	}
}

var errors = getErrors("https://raw.githubusercontent.com/matrixbotio/constants/master/errors/errors.json")

func Error(name string) *APIError {
	res, exists := errors["foo"]
	if !exists {
		return err(-1, "ERR_UNKNOWN", "Cannot get error named " + name)
	}
	return err(res.Code, res.Name, res.Message)
}
