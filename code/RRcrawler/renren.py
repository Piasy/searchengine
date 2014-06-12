# -*- coding: utf-8 -*-

import urllib2
import json
import time
retdict = {}

ACCESS = "access_token=267979|6.8565231adc0d9a3831917143588c9596.2592000.1402502400-269795344"
#ACCESS = "access_token=268293|6.b95a4f177987c35a39be7e5e32e13866.2592000.1403186400-269795344"
'''
LIST = "https://api.renren.com/v2/page/user/list"

USER = "pageSize=10&pageNumber=1&userId=279831165"

url = LIST + "?" + ACCESS + "&" + USER

response = json.loads(urllib2.urlopen(url).read())

ids = []
names = []
for r in response['response']:
    print r['id']
    ids.append((r['id'], r['name']))
'''

idlist = []
idfile = open('renren.txt', 'r')
for line in idfile:
    line = line.split()
    idlist.append((int(line[0]), line[1]))

STATUS = "https://api.renren.com/v2/status/list"

retlist = []
'''
for i in range(12, 15):
    for ownid, name in idlist:
        ID = "ownerId={}&pageSize=20&pageNumber={}".format(ownid, i)

        url = STATUS + "?" + ACCESS + "&" + ID

        print url
        time.sleep(20)
        try:
            response = json.loads(urllib2.urlopen(url).read())
        except Exception as err:
            print(err)
        finally:
            print 'a'
        for r in response['response']:
            print r['createTime']
            print r['content'].encode('utf-8')
            timeArray = time.strptime(r['createTime'], "%Y-%m-%d %H:%M:%S")
            timeint = int(time.mktime(timeArray))
            title = r['content'].encode('utf-8')
            retdict['time'] = timeint
            retdict['title'] = title
            retdict['id'] = r['id']
            retdict['url'] = "http://page.renren.com/{}/fdoing/{}".format(ownid, r['id'])
            retdict['name'] = name
            import json
            with open('data4.txt', 'a+') as outfile:
                json.dump(retdict, outfile, ensure_ascii=False)
                outfile.write('\n')
'''
BLOG = "https://api.renren.com/v2/blog/list"

for i in range(1, 10):
    for ownid, name in idlist:
        ID = "ownerId={}&pageSize=20&pageNumber={}".format(ownid, i)

        url = BLOG + "?" + ACCESS + "&" + ID

        time.sleep(10)
        try:
            response = json.loads(urllib2.urlopen(url).read())
        except Exception as err:
            print(err)
        finally:
            print 'nothing happened...'
        for r in response['response']:
            timeArray = time.strptime(r['createTime'], "%Y-%m-%d %H:%M:%S:%f")
            timeint = int(time.mktime(timeArray))
            title = r['title'].encode('utf-8')
            content = r['content'].encode('utf-8')
            retdict['time'] = timeint
            retdict['title'] = title
            retdict['content'] = content
            retdict['id'] = r['id']
            retdict['name'] = name
            retdict['url'] = "http://page.renren.com/{}/note/{}".format(ownid, r['id'])

            import json
            with open('data4blog.txt', 'a') as outfile:
                json.dump(retdict, outfile, ensure_ascii=False)
                outfile.write('\n')


'''
import json
with open('data2blog.txt', 'w') as outfile:
    json.dump(retlist, outfile, ensure_ascii=False)
    '''
