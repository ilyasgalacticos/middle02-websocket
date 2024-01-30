package kz.bitlab.middle02.middle02websocket.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatCustomMessage {

    private String content;
    private String receiver;

}
