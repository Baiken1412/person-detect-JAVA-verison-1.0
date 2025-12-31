SELECT 
    id,
    pssj AS start_time,
    jssj AS end_time,
    qyid,
    qymc,
    sxtmx,
    jscs,
    rysl,
    TIMESTAMPDIFF(SECOND, pssj, jssj) AS duration_sec
FROM app_track
ORDER BY pssj DESC
LIMIT 1;
