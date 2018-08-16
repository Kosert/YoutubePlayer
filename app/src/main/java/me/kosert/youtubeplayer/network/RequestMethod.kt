package me.kosert.youtubeplayer.network

import com.android.volley.Request

enum class RequestMethod(val id: Int)
{
	GET(Request.Method.GET),
	POST(Request.Method.POST),
	PUT(Request.Method.PUT),
	DELETE(Request.Method.DELETE),
	HEAD(Request.Method.HEAD),
	OPTIONS(Request.Method.OPTIONS);
}