<?php
/*
$data = file_get_contents('php://input');

$api_data = json_decode($data);
$zip_url = base64_url_decode($api_data->encodedurl);
$path = $api_data->path;
$siteid = $api_data->siteid;
$local_zip = "tmp/artifact_" . $siteid . ".zip";

get_artifact($zip_url, $local_zip);
unzip_artifact($local_zip, $path);
cleanup($siteid);
*/
require_once "resources.class";

$resources = array();
for($x=0;$x<20;$x++) {
    $res = array(
        "url"=>"https://resources_qa.s3.amazonaws.com/ff8081813caa19b7013caab7c4e0009e.1360073835744?Signature=fSrvyQ3CKBgZREaQ2S8113PSsmM%3D&Expires=1370732659&AWSAccessKeyId=15QD3JT20RPTAAMGSWG2",
        "path"=>"/foo_resources/" . $x . ".jpg"
    );
    array_push($resources, $res);
}
$mc = new ResourceDownloader($resources, 5);
$mc->process();

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
function fetch_resources($resources, $path){ 
     
    $mh = curl_multi_init();
    $conn = array();
    foreach ($resources as $i => $res) {

	$local_file = $res['path'];
	$local_dir = dirname($local_file);
	mkdir_p($local_dir, 755, true);
	$url = $res['url'];
        $conn[$i]=curl_init($url);
        $fp[$i]=fopen ($local_file, "w");
	curl_set_opt(CURLOPT_MAXCONNECTS, 15);
        curl_setopt ($conn[$i], CURLOPT_FILE, $fp[$i]);
        curl_setopt ($conn[$i], CURLOPT_HEADER ,0);
        curl_setopt($conn[$i],CURLOPT_CONNECTTIMEOUT,60);
        curl_multi_add_handle ($mh,$conn[$i]);
    }

    do {
        $n=curl_multi_exec($mh,$active);
    }

    while ($active);
    foreach ($resources as $i => $res) {
	echo $i . "\n";
        curl_multi_remove_handle($mh,$conn[$i]);
        curl_close($conn[$i]);
        fclose ($fp[$i]);
    }
    curl_multi_close($mh);
} 

function mkdir_p( $target ) {

	// safe mode fails with a trailing slash under certain PHP versions.
	$target = rtrim($target, '/'); // Use rtrim() instead of untrailingslashit to avoid formatting.php dependency.
	if ( empty($target) )
		$target = '/';

	if ( file_exists( $target ) )
		return @is_dir( $target );

	// Attempting to create the directory may clutter up our display.
	if ( @mkdir( $target ) ) {
		$stat = @stat( dirname( $target ) );
		$dir_perms = $stat['mode'] & 0007777;  // Get the permission bits.
		@chmod( $target, $dir_perms );
		return true;
	} elseif ( is_dir( dirname( $target ) ) ) {
			return false;
	}

	// If the above failed, attempt to create the parent node, then try again.
	if ( ( $target != '/' ) && ( mkdir_p( dirname( $target ) ) ) )
		return mkdir_p( $target );

	return false;
}


function cleanup($siteid) {
    $one_time_file = basename($_SERVER['PHP_SELF']);
    $local_zip = "tmp/artifact_" . $siteid . ".zip";
    unlink($local_zip);
    unlink($one_time_file);

}

?>
