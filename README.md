FirejailProfileFormatter
========
This is a set of three utilities in order to format Firejail profiles

Main
------
This rewrites profiles to a standard format.
This was used for the bulk of https://github.com/netblue30/firejail/pull/1427

MainN
------
This is used to sort a new option into place.
This was used for:
- https://github.com/netblue30/firejail/commit/284e0750e51bc9f9833b529eead37b42d1b223f0
- https://github.com/netblue30/firejail/commit/68fd00cfe4033a0299c481825373df696b7acdb5
- https://github.com/netblue30/firejail/commit/104dde49c0744b73ce795b9a4086607232a18305

MainWNB
------
This converts a profile to whitelist-noblacklist.
wnb was a feature proposed in https://github.com/netblue30/firejail/issues/1569
