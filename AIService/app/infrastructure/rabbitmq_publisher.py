import json
import uuid
from datetime import datetime, timezone

import aio_pika
from app.config import settings


class RabbitMQPublisher:
    """Публикует события AI-обработки в RabbitMQ."""

    def __init__(self) -> None:
        self._connection = None
        self._channel = None
        self._exchange = None

    async def connect(self) -> None:
        """Установить соединение с RabbitMQ."""
        self._connection = await aio_pika.connect_robust(
            f"amqp://{settings.rabbitmq_username}:{settings.rabbitmq_password}@"
            f"{settings.rabbitmq_host}:{settings.rabbitmq_port}/",
            timeout=10,
        )
        self._channel = await self._connection.channel()
        self._exchange = await self._channel.declare_exchange(
            settings.ai_exchange,
            aio_pika.ExchangeType.TOPIC,
            durable=True,
        )
        queue = await self._channel.declare_queue(
             "notification.queue",
             durable=True,
        )
        await queue.bind(self._exchange, routing_key=settings.summarize_routing_key)

    async def publish_summary_completed(
        self,
        note_id: str,
        summary: str,
        user_id: str = "anonymous",
    ) -> None:
        """Отправить событие о завершении суммаризации."""
        if not self._channel or self._channel.is_closed:
            await self.connect()

        assert self._exchange is not None

        payload = {
            "version": "v1",
            "event_type": "summarization.done",
            "author_id": "service:summarization-service",
            "user_id": user_id,
            "entity_id": str(uuid.uuid4()),
            "entity_type": "summarization",
            "created_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
            "meta": {
                "title": "Суммаризация готова",
                "description": f"Суммаризация для заметки {note_id} готова",
                "level": "INFO",
            },
            "payload": {
                "summarization": summary,
            },
        }

        message = aio_pika.Message(
            body=json.dumps(payload, ensure_ascii=False).encode(),
            content_type="application/json",
            delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
        )

        await self._exchange.publish(
            message,
            routing_key=settings.summarize_routing_key,
        )

    async def close(self) -> None:
        """Закрыть соединение."""
        if self._channel and not self._channel.is_closed:
            await self._channel.close()
        if self._connection and not self._connection.is_closed:
            await self._connection.close()