package kz.bitlab.middle02.middle02websocket.repository;

import kz.bitlab.middle02.middle02websocket.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}