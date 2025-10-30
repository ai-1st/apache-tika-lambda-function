# Apache Tika Lambda Function

This AWS Lambda function uses Apache Tika to extract text content from URLs. It's built with Java 21 and deployed using AWS SAM.

## Features

- Extracts text from various document formats (PDF, DOC, DOCX, TXT, HTML, etc.)
- Downloads content from provided URLs
- Returns extracted text in JSON format
- Public Lambda Function URL for easy access
- CORS enabled for web applications

## Prerequisites

- AWS CLI configured with appropriate permissions
- AWS SAM CLI installed
- Java 21 JDK
- Maven 3.6+

## Building and Deployment

### 1. Build the project

```bash
# Build the Maven project
mvn clean package

# This creates the JAR file in target/tika-lambda-1.0-jar-with-dependencies.jar
```

### 2. Deploy with SAM

```bash
# Build the SAM application
sam build

# Deploy to AWS (first time)
sam deploy --guided

# For subsequent deployments
sam deploy
```

### 3. Get the Function URL

After deployment, the Function URL will be displayed in the outputs. You can also get it using:

```bash
aws cloudformation describe-stacks --stack-name <your-stack-name> --query 'Stacks[0].Outputs'
```

## Usage

### Request Format

Send a POST request to the Lambda Function URL with the following JSON body:

```json
{
  "url": "https://example.com/document.pdf"
}
```

### Response Format

```json
{
  "url": "https://example.com/document.pdf",
  "text": "Extracted text content...",
  "contentLength": 12345,
  "success": true
}
```

### Error Response

```json
{
  "success": false,
  "error": "Error message"
}
```

## Example Usage

### Using curl

```bash
curl -X POST https://your-function-url.lambda-url.region.on.aws/ \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/document.pdf"}'
```

### Using JavaScript

```javascript
const response = await fetch('https://your-function-url.lambda-url.region.on.aws/', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    url: 'https://example.com/document.pdf'
  })
});

const result = await response.json();
console.log(result.text);
```

## Supported File Types

Apache Tika supports a wide range of file types including:

- PDF documents
- Microsoft Office documents (DOC, DOCX, XLS, XLSX, PPT, PPTX)
- OpenDocument formats (ODT, ODS, ODP)
- HTML and XML
- Plain text files
- Images with embedded text (OCR)
- And many more...

## Configuration

The Lambda function is configured with:
- **Runtime**: Java 21
- **Memory**: 1024 MB
- **Timeout**: 30 seconds
- **Function URL**: Public access with CORS enabled

## Local Testing

You can test the function locally using SAM:

```bash
# Start local API
sam local start-api

# Test with curl
curl -X POST http://localhost:3000/ \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/document.pdf"}'
```

## Troubleshooting

1. **Memory issues**: If processing large files, consider increasing the memory allocation in `template.yaml`
2. **Timeout issues**: For very large documents, increase the timeout value
3. **Network issues**: Ensure the Lambda function has internet access to download URLs
4. **CORS issues**: The function includes CORS headers, but ensure your client is sending the correct headers

## Security Considerations

- The function URL is public - consider adding authentication if needed
- The function downloads content from any URL - consider adding URL validation
- Monitor usage and costs as the function processes external content
