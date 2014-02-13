PanoEngine
==========

Panorama Viewer Engine written in several languages

The engine is based heavily off of panoramagl (https://code.google.com/p/panoramagl/)
and panoramagl-android (https://code.google.com/p/panoramagl-android/). However, I
found those libraries a bit too cumbersome for my purposes.

To Use
------
Add a ```PanoramaView```, via code or GUI builder.
Then, using your ```PanoramaLoader``` (default implementation: ```JSONPanoramaLoader```), load the panorama
when the view becomes visible.

You will need to implement the ```BitmapLoader``` interface, to follow decode your
AssetID scheme and load ```Bitmap```s

JSON Protocol
-------------
*For Cubic Panoramas*
```json
{
    "type": "cubic",		//Panorama type. Accepts: "cubic" or "cylindrical"
	"subdivisionX": [int],	//Number of horizontal subdivions
	"subdivisionY": [int],	//Number of vertical subdivisions
	"scrolling":			//Touch-based movement (Optional)
	{
		"enabled": [bool]
	},
    "gyro":					//Gyroscope-based movement (Optional)
	{
		"enabled": [bool]
	},
    "images":				//Panoramic images section
    {
        "preview": [string],	//Preview image AssetID, displays until regular images are loaded (Optional)
        "front":			//Front images
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		],
        "back":				//Back images
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		],
        "left":				//Left images
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		],
        "right":			//Right images
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		],
        "up":				//Up images
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		],
        "down":				//Down images
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		]
    },
    "camera":				//Camera settings section (Optional)
    {
        "vlookat": [int],	//Initial vertical position [-90, 90]
        "hlookat": [int],	//Initial horizontal position [-180, 180]
        "atvmin": [int],	//Min vertical position [-90, 90]
        "atvmax": [int],	//Max vertical position [-90, 90]
        "athmin": [int],	//Min horizontal position [-180, 180]
        "athmax": [int]		//Max horizontal position [-180, 180]
    },
    "hotspots":				//Hotspots section (Optional, this section is an array of hotspots)
	[
		 {
		 "id": [int],	//Hotspot identifier (int)
		 "atv": [int],	//Vertical position [-90, 90]
		 "ath": [int],	//Horizontal position [-180, 180]
		 "width": [float],	//Width
		 "height": [float],	//Height
		 "image": [string]	//Image
		 }
	]
}
```

*For Cylindrical Panoramas*
```json
{
    "type": "cylindrical",		//Panorama type. Accepts: "cubic" or "cylindrical"
	"subdivisionX": [int],	//Number of horizontal subdivions
	"subdivisionY": [int],	//Number of vertical subdivisions
	"scrolling":			//Touch-based movement (Optional)
	{
		"enabled": [bool]
	},
    "gyro":					//Gyroscope-based movement (Optional)
	{
		"enabled": [bool]
	},
    "images":				//Panoramic images section
    {
        "preview": [string],	//Preview image AssetID, displays until regular images are loaded (Optional)
        "images":			//Images around the panorama
		[
			{
				"divX": [int],	//vertical subdivision index
				"divY": [int],	//horizontal subdivision index
				"assetID": [string]	//Image AssetID
			},
			...
		]
    },
    "camera":				//Camera settings section (Optional)
    {
        "vlookat": [int],	//Initial vertical position [-90, 90]
        "hlookat": [int],	//Initial horizontal position [-180, 180]
        "atvmin": [int],	//Min vertical position [-90, 90]
        "atvmax": [int],	//Max vertical position [-90, 90]
        "athmin": [int],	//Min horizontal position [-180, 180]
        "athmax": [int]		//Max horizontal position [-180, 180]
    },
    "hotspots":
	[			//Hotspots section (Optional, this section is an array of hotspots)
		 {
		 "id": [int],	//Hotspot identifier (int)
		 "atv": [int],	//Vertical position [-90, 90]
		 "ath": [int],	//Horizontal position [-180, 180]
		 "width": [float],	//Width
		 "height": [float],	//Height
		 "image": [string]	//Image
		 }
	]
}
```
