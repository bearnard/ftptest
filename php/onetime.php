<?php

$data = file_get_contents('php://input');

$api_data = json_decode($data);
$zip_url = base64_url_decode($api_data->encodedurl);
$path = $api_data->path;
$siteid = $api_data->siteid;
$local_zip = "tmp/artifact_" . $siteid . ".zip";

get_artifact($zip_url, $local_zip);
unzip_artifact($local_zip, $path);
cleanup($siteid);



function base64_url_decode($input)
{
    return base64_decode(strtr($input, '-_,', '+/='));
}

function get_artifact($url, $local_file) {

    set_time_limit(0);
    $fp = fopen ($local_file, 'w+'); // Where to save the site artifact.
    $ch = curl_init(str_replace(" ","%20",$url)); // Artifact url, replace spaces with %20
    curl_setopt($ch, CURLOPT_TIMEOUT, 35);
    curl_setopt($ch, CURLOPT_FILE, $fp); // to file instead of in memory.
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_exec($ch);
    $info = curl_getinfo($ch);
    curl_close($ch);
    fclose($fp);
    if ($info['http_code'] == 200) {
        return true;
    }
    return false;
}

function unzip_artifact($local_file, $path) {

    $zip = new ZipArchive;
    if ($zip->open($local_file) === TRUE) {
        $zip->extractTo($path);
        $zip->close();

    } else {
        return false;
    }
}

function cleanup($siteid) {
    $one_time_file = basename($_SERVER['PHP_SELF']);
    $local_zip = "tmp/artifact_" . $siteid . ".zip";
    unlink($local_zip);
    unlink($one_time_file);

}

?>
