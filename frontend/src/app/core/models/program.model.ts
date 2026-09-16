import { PlanContent } from './plan-content.model';

export type ProgramState = 'NONE' | 'CHANGES_REQUESTED' | 'IN_REVIEW' | 'ACTIVE';

export interface MyProgramDto {
  state: ProgramState;
  programStatus: string;
  versionNo: number | null;
  submittedAt: string | null;
  coachNote: string | null;
  content: PlanContent | null;
}

export interface ProgramVersionSummary {
  id: number;
  versionNo: number;
  status: string;
  createdBy: string;
  changelog: string[];
  coachNote: string | null;
  current: boolean;
  createdAt: string;
  activatedAt: string | null;
}

export interface ProgramVersionDetail {
  id: number;
  versionNo: number;
  status: string;
  content: PlanContent;
  changelog: string[];
  coachNote: string | null;
  createdAt: string;
  activatedAt: string | null;
}

export interface ChangeFieldVm {
  field: string;
  label: string;
}

export interface ChangeRequestDto {
  programId: number;
  note: string | null;
  fields: ChangeFieldVm[];
  prefill: Record<string, string>;
}
