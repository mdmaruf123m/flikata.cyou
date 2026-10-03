<?php
/**
 * Cloudflare R2 S3-Compatible Multi-Bucket Helper (Pure PHP AWS SigV4)
 * Supports dynamic bucket selection, storage limit rollover, testing, and uploads.
 */

class CloudflareR2 {

    /**
     * Generate AWS Signature Version 4 Headers for Cloudflare R2 S3 API
     */
    public static function createSigV4Headers($method, $uri, $queryString, $headers, $payloadHash, $accessKey, $secretKey, $region = 'auto') {
        $timestamp = gmdate('Ymd\THis\Z');
        $dateStamp = gmdate('Ymd');
        $service = 's3';

        $headers['x-amz-date'] = $timestamp;
        $headers['x-amz-content-sha256'] = $payloadHash;

        // Canonical headers
        $canonicalHeaders = [];
        $signedHeadersList = [];
        foreach ($headers as $k => $v) {
            $lower = strtolower(trim($k));
            $canonicalHeaders[$lower] = trim($v);
            $signedHeadersList[] = $lower;
        }
        sort($signedHeadersList);
        ksort($canonicalHeaders);

        $canonicalHeadersStr = '';
        foreach ($canonicalHeaders as $k => $v) {
            $canonicalHeadersStr .= $k . ':' . $v . "\n";
        }
        $signedHeadersStr = implode(';', $signedHeadersList);

        $canonicalRequest = $method . "\n"
            . $uri . "\n"
            . $queryString . "\n"
            . $canonicalHeadersStr . "\n"
            . $signedHeadersStr . "\n"
            . $payloadHash;

        $credentialScope = $dateStamp . '/' . $region . '/' . $service . '/aws4_request';
        $stringToSign = "AWS4-HMAC-SHA256\n"
            . $timestamp . "\n"
            . $credentialScope . "\n"
            . hash('sha256', $canonicalRequest);

        // Calculate signature key
        $kSecret = 'AWS4' . $secretKey;
        $kDate = hash_hmac('sha256', $dateStamp, $kSecret, true);
        $kRegion = hash_hmac('sha256', $region, $kDate, true);
        $kService = hash_hmac('sha256', $service, $kRegion, true);
        $kSigning = hash_hmac('sha256', 'aws4_request', $kService, true);
        $signature = hash_hmac('sha256', $stringToSign, $kSigning);

        $authorization = "AWS4-HMAC-SHA256 Credential={$accessKey}/{$credentialScope}, SignedHeaders={$signedHeadersStr}, Signature={$signature}";
        $headers['Authorization'] = $authorization;

        return $headers;
    }

    /**
     * Test connection to Cloudflare R2 bucket
     */
    public static function testBucketConnection($bucket) {
        $accountId = trim($bucket['account_id']);
        $bucketName = trim($bucket['bucket_name']);
        $accessKey = trim($bucket['access_key_id']);
        $secretKey = trim($bucket['secret_access_key']);
        $region = 'auto';

        $host = "{$accountId}.r2.cloudflarestorage.com";
        $testFileKey = ".flikata_test_ping.txt";
        $uri = "/{$bucketName}/{$testFileKey}";
        $content = "Flikata R2 Connection Test @ " . date('Y-m-d H:i:s');
        $payloadHash = hash('sha256', $content);

        $headers = [
            'Host'           => $host,
            'Content-Type'   => 'text/plain',
            'Content-Length' => (string)strlen($content)
        ];

        $signedHeaders = self::createSigV4Headers('PUT', $uri, '', $headers, $payloadHash, $accessKey, $secretKey, $region);

        $formattedHeaders = [];
        foreach ($signedHeaders as $k => $v) {
            $formattedHeaders[] = "{$k}: {$v}";
        }

        $url = "https://{$host}{$uri}";
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
        curl_setopt($ch, CURLOPT_POSTFIELDS, $content);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $formattedHeaders);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, 15);

        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $curlError = curl_error($ch);
        curl_close($ch);

        if ($curlError) {
            return [
                'success' => false,
                'message' => 'Network error: ' . $curlError
            ];
        }

        if ($httpCode >= 200 && $httpCode < 300) {
            return [
                'success' => true,
                'message' => "Success! Successfully connected to bucket '{$bucketName}'. HTTP {$httpCode} OK."
            ];
        } else {
            return [
                'success' => false,
                'message' => "Authentication or configuration failed (HTTP {$httpCode}). Response: " . strip_tags($response)
            ];
        }
    }

    /**
     * Upload binary or file stream to Cloudflare R2
     */
    public static function uploadFile($bucket, $fileData, $targetKey, $contentType) {
        $accountId = trim($bucket['account_id']);
        $bucketName = trim($bucket['bucket_name']);
        $accessKey = trim($bucket['access_key_id']);
        $secretKey = trim($bucket['secret_access_key']);
        $publicUrl = rtrim(trim($bucket['public_url']), '/');
        $region = 'auto';

        $host = "{$accountId}.r2.cloudflarestorage.com";
        $uri = "/{$bucketName}/" . ltrim($targetKey, '/');
        $payloadHash = hash('sha256', $fileData);
        $fileSize = strlen($fileData);

        $headers = [
            'Host'           => $host,
            'Content-Type'   => $contentType,
            'Content-Length' => (string)$fileSize
        ];

        $signedHeaders = self::createSigV4Headers('PUT', $uri, '', $headers, $payloadHash, $accessKey, $secretKey, $region);

        $formattedHeaders = [];
        foreach ($signedHeaders as $k => $v) {
            $formattedHeaders[] = "{$k}: {$v}";
        }

        $url = "https://{$host}{$uri}";
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
        curl_setopt($ch, CURLOPT_POSTFIELDS, $fileData);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $formattedHeaders);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, 60);

        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $curlError = curl_error($ch);
        curl_close($ch);

        if ($curlError) {
            return [
                'success' => false,
                'message' => 'R2 cURL error: ' . $curlError
            ];
        }

        if ($httpCode >= 200 && $httpCode < 300) {
            $fileUrl = $publicUrl . '/' . ltrim($targetKey, '/');
            return [
                'success'   => true,
                'file_url'  => $fileUrl,
                'file_size' => $fileSize,
                'bucket_id' => $bucket['id']
            ];
        } else {
            return [
                'success' => false,
                'message' => "R2 upload failed (HTTP {$httpCode}): " . strip_tags($response)
            ];
        }
    }

    /**
     * Select next available active bucket with available quota
     */
    public static function getAvailableBucket($pdo, $requiredBytes = 0) {
        // Query active buckets ordered by priority and available capacity
        $stmt = $pdo->prepare("
            SELECT * FROM r2_buckets 
            WHERE is_active = 1 
            ORDER BY priority ASC, id ASC
        ");
        $stmt->execute();
        $buckets = $stmt->fetchAll();

        foreach ($buckets as $bucket) {
            $limitBytes = (float)$bucket['account_limit_gb'] * 1024 * 1024 * 1024;
            $usedBytes = (float)$bucket['used_bytes'];

            if (($usedBytes + $requiredBytes) <= $limitBytes) {
                return $bucket;
            }
        }

        return null;
    }

    /**
     * Increment used bytes for bucket in DB
     */
    public static function incrementBucketUsage($pdo, $bucketId, $bytes) {
        $stmt = $pdo->prepare("UPDATE r2_buckets SET used_bytes = used_bytes + ? WHERE id = ?");
        $stmt->execute([$bytes, $bucketId]);
    }
}
