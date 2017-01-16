package test_package

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestWork(t *testing.T) {

	result := Function()

	assert.Equal("Function", result, "It's not working")

}
