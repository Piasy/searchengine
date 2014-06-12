from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'search.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),
    url(r'^$', 'search.proxy.homepage'),
    url(r'^api/search/$', 'search.proxy.search'),
    url(r'^api/top/$', 'search.proxy.top'),
    url(r'^api/related/$', 'search.proxy.related'),
    url(r'^admin/', include(admin.site.urls)),
)
