# -*- encoding: utf-8 -*-
from django.shortcuts import render
from django.http import HttpResponse
import urllib
import json

def homepage(request):
    return HttpResponse('<script language="javascript" type="">window.location.href="/static/web/search.html"</script>')

def search(request):
    url = 'http://piasy.luyunyi.com:5020/search'
    q = request.GET['query']
    f = request.GET['from']
    u = request.GET['until']
    params = 'from=%s&until=%s&query=%s' % (f, u, q)
    res = '%s?%s' % (url, params)
    res = res.encode('utf-8')
    page = urllib.urlopen(res)
    return HttpResponse(page)

def top(request):
    url = 'http://piasy.luyunyi.com:5020/top'
    page = urllib.urlopen(url)
    return HttpResponse(page)

def related(request):
    url = 'http://piasy.luyunyi.com:5020/related'
    q = request.GET['query']
    params = 'query=%s' % (q, )
    res = '%s?%s' % (url, params)
    res = res.encode('utf-8')
    page = urllib.urlopen(res)
    now = json.loads(page.read())
    result = {'query': q, 'data':now['related'], 'suggestions':now['related']}
    return HttpResponse(json.dumps(result))
