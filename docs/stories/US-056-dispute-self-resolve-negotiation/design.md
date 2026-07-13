# Design

`dispute_self_resolve_replies` is append-only negotiation history. Replies retain the actor account internally, but the response DTO exposes only role and display name.

Agreement creates an `ACCEPT_PROPOSAL` timeline entry linked to the accepted reply. The service locks the dispute and matching contract milestone to prevent two agreement actions from settling the same escrow.

`CONTINUE_REVISION` sets both milestone records to `IN_PROGRESS`. The two approval-style actions reuse the ledger operation pattern used by milestone approval, but use a distinct `SELF_RESOLVE_AGREEMENT` operation key and settlement source.
