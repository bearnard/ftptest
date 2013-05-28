from gevent import monkey
monkey.patch_all()
from gevent.pool import Pool
from gevent.queue import JoinableQueue
from gevent import spawn, joinall

import os
import ftputil
import ftptool
import zipfile
import sys

siteid = ''
path = '/upload/'

host = 'foo'
user = 'ftptest'
password = 's3cr3t'

dirs_made = []

def upload_files(i, w, q):
    with ftputil.FTPHost(host, user, password) as ftphost:
        while True:
            f = q.get(block=True)
            print "FILE", f
            ftphost.upload(f, os.path.join('upload', f))
            q.task_done()


def mkdirs(d):

    with ftputil.FTPHost(host, user, password) as ftphost:
        try:
            ftphost.mkdir(os.path.join('upload', d))
            print "SUCCESS:", d
        except:
            print "FAILED:", d


if __name__ == '__main__':

    if len(sys.argv) < 3:
        sys.exit('Usage: %s worker_id concurrency' % sys.argv[0])

    worker_id = sys.argv[1]
    concurrency = int(sys.argv[2])
    site_id = sys.argv[3]
    #file_queue = JoinableQueue(maxsize=concurrency)
    file_queue = JoinableQueue()
    pool = Pool(concurrency)
    dir_pool = Pool(concurrency)

    zfile = '%s.zip' % site_id
    with zipfile.ZipFile(zfile) as z:
        z.extractall('tmp')

    file_workers = [
        pool.spawn(upload_files, i, worker_id, file_queue) for i in xrange(concurrency)
    ]

    for dirname, dirnames, filenames in os.walk('tmp/%s' % site_id):
        # print path to all subdirectories first.
        files = []
        for filename in filenames:
            files.append(os.path.join(dirname, filename))
        for f in files:
            file_queue.put(f, block=False)
        print "START_DIRS"
        dirs = []
        for subdirname in dirnames:
            dirs.append(os.path.join(dirname, subdirname))
        if dirs:
            print "POOLING:", dirs
            dir_pool.imap(mkdirs, dirs)
        print "END"
    #joinall(dir_jobs)
    #joinall([
    #    spawn([s_dir] + dirs) for s_dir, dirs in skel_dirs.iteritems()
    #])

    file_queue.join()
