import asyncio
from datetime import timedelta
from temporalio import workflow
from temporalio.common import RetryPolicy

with workflow.unsafe.imports_passed_through():
    from activities import process_ocr_activity, index_document_activity


@workflow.definition(name="SaveNoteWorkflow")
class SaveNoteWorkflow:
    @workflow.run
    async def run(self, note_id: str, user_id: str, base_text: str, file_urls: list[str]) -> dict:
        ocr_retry_policy = RetryPolicy(
            initial_interval=timedelta(seconds=5),
            backoff_coefficient=2.0,
            maximum_interval=timedelta(seconds=60),
            maximum_attempts=5
        )

        index_retry_policy = RetryPolicy(
            initial_interval=timedelta(seconds=2),
            backoff_coefficient=2.0,
            maximum_attempts=3
        )

        initial_index_payload = {
            "note_id": note_id,
            "user_id": user_id,
            "text": base_text
        }
        index_task = workflow.execute_activity(
            index_document_activity,
            initial_index_payload,
            start_to_close_timeout=timedelta(seconds=30),
            retry_policy=index_retry_policy
        )

        ocr_task = workflow.execute_activity(
            process_ocr_activity,
            file_urls,
            start_to_close_timeout=timedelta(minutes=5),
            retry_policy=ocr_retry_policy
        )

        _, ocr_result_text = await asyncio.gather(index_task, ocr_task)

        if ocr_result_text:
            final_text = f"{base_text}\n{ocr_result_text}".strip()

            final_index_payload = {
                "note_id": note_id,
                "user_id": user_id,
                "text": final_text
            }

            await workflow.execute_activity(
                index_document_activity,
                final_index_payload,
                start_to_close_timeout=timedelta(seconds=30),
                retry_policy=index_retry_policy
            )
            return {"status": "SUCCESS", "indexed_with_ocr": True}

        return {"status": "SUCCESS", "indexed_with_ocr": False}