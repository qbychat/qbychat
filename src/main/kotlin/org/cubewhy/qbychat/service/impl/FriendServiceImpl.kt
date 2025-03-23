import com.google.protobuf.ByteString
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.qbychat.entity.*
import org.cubewhy.qbychat.repository.FriendRepository
import org.cubewhy.qbychat.repository.UserRepository
import org.cubewhy.qbychat.service.FriendService
import org.cubewhy.qbychat.service.UserMapper
import org.cubewhy.qbychat.util.toProtobufType
import org.cubewhy.qbychat.websocket.friend.WebsocketFriend
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class FriendServiceImpl(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) : FriendService {
    override suspend fun processSyncRequest(
        user: User
    ): WebsocketResponse {
        val friend = findFriend(user)
        return websocketResponseOf(WebsocketFriend.SyncResponse.newBuilder().apply {
            addAllFriends(friend.map { friend -> friend.protoType })
        }.build())
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User?
    ): WebsocketResponse {
        return when (method) {
            "Sync" -> this.processSyncRequest(
                user!!
            )

            else -> emptyWebsocketResponse()
        }
    }

    private fun buildFriendResponse(targetUser: User, friend: Friend): WebsocketFriend.Friend {
        return WebsocketFriend.Friend.newBuilder().apply {
            setFriend(userMapper.fullUserVO(targetUser))
            timestamp = friend.timestamp.toProtobufType()
        }.build()
    }

    private suspend fun findFriend(user: User): List<InternalFriendDTO> {
        return friendRepository.findFriendRelations(user.id!!)
            .flatMap { friend ->
                userRepository.findById(friend.getTargetId(user))
                    .map { targetUser ->
                        InternalFriendDTO(
                            protoType = buildFriendResponse(targetUser, friend),
                            friendUser = targetUser
                        )
                    }
            }.collectList().awaitLast()
    }

    private data class InternalFriendDTO(
        val protoType: WebsocketFriend.Friend,
        val friendUser: User
    )
}