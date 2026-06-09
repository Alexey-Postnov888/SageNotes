import asyncio
from temporalio.client import Client
from temporalio.worker import Worker
import os

from activities import process_ocr_activity, index_document_activity
from workflows import SaveNoteWorkflow


async def main():
    temporal_host = os.getenv("TEMPORAL_HOST")

    client = await Client.connect(temporal_host)

    worker = Worker(
        client,
        task_queue="content-task-queue",
        workflows=[SaveNoteWorkflow],
        activities=[process_ocr_activity, index_document_activity],
    )

    print("Content Service Temporal Worker started...")
    await worker.run()


if __name__ == "__main__":
    asyncio.run(main())