#!/bin/bash

# Test script for the Tika Lambda Function
# Usage: ./test.sh <function-url> <test-url>
# Output: Written to output.txt

if [ $# -ne 2 ]; then
    echo "Usage: $0 <function-url> <test-url>"
    echo "Example: $0 https://abc123.lambda-url.us-east-1.on.aws/ https://example.com/document.pdf"
    exit 1
fi

FUNCTION_URL=$1
TEST_URL=$2
OUTPUT_FILE="output.txt"

echo "Testing Tika Lambda Function..."
echo "Function URL: $FUNCTION_URL"
echo "Test URL: $TEST_URL"
echo "Output will be written to: $OUTPUT_FILE"
echo ""

# Test the function and write output to file
{
  echo "=== Test Information ==="
  echo "Function URL: $FUNCTION_URL"
  echo "Test URL: $TEST_URL"
  echo "Timestamp: $(date)"
  echo ""
  echo "=== Lambda Response ==="
  
  curl -X POST "$FUNCTION_URL" \
    -H "Content-Type: application/json" \
    -d "{\"url\": \"$TEST_URL\"}" \
    -w "\n\nHTTP Status: %{http_code}\nTotal Time: %{time_total}s\n" \
    -s
  
  echo ""
  echo "=== Test Completed ==="
} > "$OUTPUT_FILE" 2>&1

echo "Test completed! Output written to $OUTPUT_FILE"
