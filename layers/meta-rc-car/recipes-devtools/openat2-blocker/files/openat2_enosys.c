/*
 * LD_PRELOAD shim that makes the openat2() syscall fail with ENOSYS.
 *
 * The host pseudo-native pinned by poky (1.9.0+git) has no wrapper for
 * openat2(), so any directory fd it returns is invisible to pseudo's
 * fd-tracking database. GNU tar >= 1.34 uses openat2()/RESOLVE_BENEATH
 * for directory-fd caching whenever the host kernel supports it (Linux
 * >= 5.6), and once pseudo loses track of that fd, the next wrapped
 * *at() call on it corrupts, breaking do_package's tar-based
 * image->package copy ("got *at() syscall for unknown directory").
 *
 * glibc has no named openat2() wrapper symbol (confirmed: absent from
 * both /lib/x86_64-linux-gnu/libc.so.6's dynamic symbol table and
 * tar's own undefined-symbol list), so tar reaches it via the generic
 * syscall(SYS_openat2, ...) entry point instead. That means a plain
 * LD_PRELOAD override of a function named "openat2" is never called -
 * there's no symbol lookup for that name to interpose on. The syscall()
 * function itself has to be intercepted instead, filtering out just
 * SYS_openat2 and forwarding everything else to the real syscall(), so
 * tar's openat2() probe fails and it permanently falls back to plain
 * openat()/mkdirat(), which pseudo already wraps correctly.
 */
#define _GNU_SOURCE
#include <dlfcn.h>
#include <errno.h>
#include <stdarg.h>
#include <stddef.h>
#include <sys/syscall.h>

static long (*real_syscall)(long, ...) = NULL;

long syscall(long number, ...)
{
	if (number == SYS_openat2) {
		errno = ENOSYS;
		return -1;
	}

	if (!real_syscall)
		real_syscall = dlsym(RTLD_NEXT, "syscall");

	va_list ap;
	long a1, a2, a3, a4, a5, a6;
	va_start(ap, number);
	a1 = va_arg(ap, long);
	a2 = va_arg(ap, long);
	a3 = va_arg(ap, long);
	a4 = va_arg(ap, long);
	a5 = va_arg(ap, long);
	a6 = va_arg(ap, long);
	va_end(ap);

	return real_syscall(number, a1, a2, a3, a4, a5, a6);
}
