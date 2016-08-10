# Java-P2P-app
java based P2P file sync app:
create two directories at first, name them whatever you like.such as "sync_active" and "sync_passive".
compile java programs provided, run the TCPPassive at first with the command:"java TCPPassivePeer folder_path "
then run the TCPActivePeer with similar command as above(modify the path according the filename created.) Command format for running TCPActivePeer:" java TCPActivePeer IP_address_of_PassivePeer folder_path"
